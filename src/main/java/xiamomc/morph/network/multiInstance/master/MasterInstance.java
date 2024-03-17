package xiamomc.morph.network.multiInstance.master;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.java_websocket.WebSocket;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.network.ReasonCodes;
import xiamomc.morph.network.commands.CommandRegistries;
import xiamomc.morph.network.multiInstance.IInstanceService;
import xiamomc.morph.network.multiInstance.protocol.IClientHandler;
import xiamomc.morph.network.multiInstance.protocol.Operation;
import xiamomc.morph.network.multiInstance.protocol.ProtocolLevel;
import xiamomc.morph.network.multiInstance.protocol.c2s.MIC2SCommand;
import xiamomc.morph.network.multiInstance.protocol.c2s.MIC2SDisguiseMetaCommand;
import xiamomc.morph.network.multiInstance.protocol.s2c.MIS2CCommand;
import xiamomc.morph.network.multiInstance.protocol.c2s.MIC2SLoginCommand;
import xiamomc.morph.network.multiInstance.protocol.s2c.MIS2CDisconnectCommand;
import xiamomc.morph.network.multiInstance.protocol.s2c.MIS2CSyncMetaCommand;
import xiamomc.morph.network.multiInstance.protocol.s2c.MIS2CLoginResultCommand;
import xiamomc.morph.network.server.MorphClientHandler;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

public class MasterInstance extends MorphPluginObject implements IInstanceService, IClientHandler
{
    @Nullable
    private InstanceServer bindingServer;

    @Resolved
    private MorphConfigManager config;

    /**
     * @return Success?
     */
    private boolean stopServer()
    {
        try
        {
            if (bindingServer != null)
            {
                bindingServer.dispose();
                bindingServer.stop(1000, "Master instance shutting down");
            }

            bindingServer = null;

            return true;
        }
        catch (Throwable t)
        {
            logger.error("Error occurred shutting down socket server: " + t.getMessage());
            t.printStackTrace();

            return false;
        }
    }

    /**
     * @return Whether this operation operates successfully
     */
    private boolean prepareServer()
    {
        if (!stopServer())
            return false;

        try
        {
            String[] configuredAddress = config.getOrDefault(String.class, ConfigOption.MASTER_ADDRESS).split(":");

            String host = configuredAddress[0];
            int port = Integer.parseInt( configuredAddress.length >= 2 ? configuredAddress[1] : "39210" );
            var addr = new InetSocketAddress(InetAddress.getByName(host), port);

            bindingServer = new InstanceServer(plugin, addr, this);
            bindingServer.start();

            return true;
        }
        catch (Throwable t)
        {
            logger.warn("Error occurred while setting up server:" + t.getMessage());
            t.printStackTrace();

            return false;
        }
    }

    private final Bindable<String> secret = new Bindable<>(null);

    @Initializer
    private void load()
    {
        logger.info("Preparing multi-instance service...");

        config.bind(secret, ConfigOption.MASTER_SECRET);

        registries.registerC2S("login", MIC2SLoginCommand::from)
                .registerC2S("dmeta", MIC2SDisguiseMetaCommand::from);

        if (!prepareServer())
        {
            logger.error("Can't setup server, not enabling multi-instance service!");
            return;
        }
    }

    //region IInstanceService

    @Override
    public boolean stop()
    {
        return stopServer();
    }

    //endregion

    private void onText(InstanceServer.WsRecord record)
    {
        var ws = record.socket();
        var text = record.rawMessage().split(" ", 2);

        logger.info("Get command from " + ws.getRemoteSocketAddress().toString() + ": " + record.rawMessage());

        var cmd = registries.createC2SCommand(text[0], text.length == 2 ? text[1] : "");
        if (cmd == null)
        {
            logger.warn("Unknown command: " + text[0]);
            return;
        }

        if (!(cmd instanceof MIC2SCommand<?> mic2s))
        {
            logger.warn("Command is not a MIC2S instance!");
            return;
        }

        mic2s.setSourceSocket(ws);
        mic2s.onCommand(this);
    }

    //region IClientHandler

    private final CommandRegistries registries = new CommandRegistries();

    private final ProtocolLevel level = ProtocolLevel.V1;

    private final List<WebSocket> allowedSockets = new ObjectArrayList<>();

    @ApiStatus.Internal
    public void broadcastCommand(MIS2CCommand<?> command)
    {
        for (var allowedSocket : this.allowedSockets)
            this.sendCommand(allowedSocket, command);
    }

    private void sendCommand(WebSocket socket, MIS2CCommand<?> command)
    {
        if (silent) return;

        if (!socket.isOpen())
        {
            logger.warn("Not sending a command to a closed socket!");
            return;
        }

        //logger.info("%s :: -> :: %s".formatted(socket.getRemoteSocketAddress(), command.buildCommand()));

        socket.send(command.buildCommand());
    }

    private void disconnect(WebSocket socket, String reason)
    {
        this.sendCommand(socket, new MIS2CDisconnectCommand(ReasonCodes.DISCONNECT, reason));
        socket.close();
    }

    private boolean socketAllowed(WebSocket socket)
    {
        return allowedSockets.contains(socket);
    }

    @Override
    public void onLoginCommand(MIC2SLoginCommand cProtocolCommand)
    {
        logger.info("'%s' is requesting a login!".formatted(cProtocolCommand.getSocket()));

        var socket = cProtocolCommand.getSocket();
        if (socket == null)
            return;

        logger.info("Level is '%s', and their secret is '%s'".formatted(cProtocolCommand.getVersion(), cProtocolCommand.getSecret()));

        if (!this.level.equals(cProtocolCommand.getVersion()))
        {
            logger.info("Protocol mismatch!");
            this.disconnect(socket, "Protocol mismatch!");
            return;
        }

        if (cProtocolCommand.getSecret() == null || !cProtocolCommand.getSecret().equals(this.secret.get()))
        {
            logger.info("Invalid secret! Disconnecting...");

            disconnect(socket, "Invalid secret '%s'".formatted(cProtocolCommand.getSecret()));
            return;
        }

        allowedSockets.add(socket);
        sendCommand(socket, new MIS2CLoginResultCommand(true));

        var cmds = new ObjectArrayList<MIS2CSyncMetaCommand>();
        var disguises = disguiseManager.listAllMeta();
        for (var meta : disguises)
        {
            var identifiers = meta.getUnlockedDisguiseIdentifiers();

            if (!identifiers.isEmpty())
                cmds.add(new MIS2CSyncMetaCommand(Operation.ADD_IF_ABSENT, identifiers, meta.uniqueId));
        }

        cmds.forEach(cmd -> this.sendCommand(socket, cmd));
    }

    private final NetworkDisguiseManager disguiseManager = new NetworkDisguiseManager();

    @Override
    public void onDisguiseMetaCommand(MIC2SDisguiseMetaCommand cDisguiseMetaCommand)
    {
        var socket = cDisguiseMetaCommand.getSocket();
        if (!socketAllowed(socket))
            return;

        assert socket != null;

        var meta = cDisguiseMetaCommand.getMeta();
        if (meta == null || !meta.isValid())
        {
            logger.warn("Got invalid meta from '%s'".formatted(socket.getRemoteSocketAddress()));
            return;
        }

        var operation = meta.getOperation();
        var identifiers = meta.getIdentifiers();

        var playerMeta = disguiseManager.getPlayerMeta(Bukkit.getOfflinePlayer(Objects.requireNonNull(meta.getBindingUuid(), "???")));
        var unlocked = playerMeta.getUnlockedDisguises();

        var player = Bukkit.getPlayer(meta.getBindingUuid());

        if (operation == Operation.ADD_IF_ABSENT)
        {
            identifiers.forEach(id ->
            {
                var disguiseMeta = disguiseManager.getDisguiseMeta(id);

                if (!unlocked.contains(disguiseMeta))
                    playerMeta.addDisguise(disguiseMeta);
            });

            // Broadcast to all allow sockets
            for (var allowedSocket : this.allowedSockets)
            {
                if (allowedSocket == cDisguiseMetaCommand.getSocket())
                    continue;

                this.sendCommand(allowedSocket, new MIS2CSyncMetaCommand(meta));
            }
        }
        else if (operation == Operation.REMOVE)
        {
            identifiers.forEach(id ->
            {
                var disguiseMeta = disguiseManager.getDisguiseMeta(id);

                playerMeta.removeDisguise(disguiseMeta);
            });

            // Broadcast to all allow sockets
            for (var allowedSocket : this.allowedSockets)
            {
                if (allowedSocket == cDisguiseMetaCommand.getSocket())
                    continue;

                this.sendCommand(allowedSocket, new MIS2CSyncMetaCommand(meta));
            }
        }
    }

    @Override
    public void onMessage(InstanceServer.WsRecord wsRecord, InstanceServer server)
    {
        this.addSchedule(() -> this.onText(wsRecord));
    }

    //endregion

    //region Utilities

    private boolean silent = false;

    //endregion
}
