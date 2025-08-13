package xyz.nifeather.morph.network.multiInstance.master;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.bukkit.Bukkit;
import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.config.ConfigOption;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.network.multiInstance.IInstanceService;
import xyz.nifeather.morph.network.multiInstance.protocol.*;
import xyz.nifeather.morph.network.multiInstance.protocol.c2s.MIC2SLoginCommand;
import xyz.nifeather.morph.network.multiInstance.protocol.c2s.MIC2SRequestSyncCommand;
import xyz.nifeather.morph.network.multiInstance.protocol.c2s.MIC2SSyncDisguiseCommand;
import xyz.nifeather.morph.network.multiInstance.protocol.s2c.*;
import xyz.nifeather.morph.network.multiInstance.slave.SlaveInstance;
import xyz.nifeather.morph.storage.playerdata.PlayerMeta;

import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MasterInstance extends MorphPluginObject implements IInstanceService, IInstanceClientHandler
{
    @Nullable
    private InstanceServer bindingServer;

    @Resolved
    private MorphConfigManager config;

    private void logMasterInfo(String message)
    {
        logger.info("[Master@%s] %s".formatted(Integer.toHexString(this.hashCode()), message));
    }

    private void logMasterWarn(String message)
    {
        logger.warn("[Master@%s] %s".formatted(Integer.toHexString(this.hashCode()), message));
    }

    /**
     * @return Success?
     */
    private boolean stopServer()
    {
        try
        {
            if (bindingServer != null)
            {
                logMasterInfo("Stopping WebSocket server...");

                bindingServer.stop(10000, "Master instance shutting down");
                bindingServer.dispose();

                logMasterInfo("Done stopping WebSocket server");
            }

            bindingServer = null;

            if (onStop != null)
                onStop.run();

            return true;
        }
        catch (Throwable t)
        {
            logMasterWarn("Error occurred shutting down socket server: " + t.getMessage());
            t.printStackTrace();

            return false;
        }
    }

    public boolean isOnline()
    {
        return bindingServer != null && bindingServer.running;
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
            logMasterWarn("Error occurred while setting up server:" + t.getMessage());
            t.printStackTrace();

            return false;
        }
    }

    private final Bindable<String> secret = new Bindable<>(null);
    private final Bindable<Boolean> debug_output = new Bindable<>(false);

    @Initializer
    private void load()
    {
        logger.info("Preparing multi-instance server...");

        config.bind(secret, ConfigOption.MASTER_SECRET);

        registries.registerC2S("login", MIC2SLoginCommand::fromArguments)
                .registerC2S("dmeta", MIC2SSyncDisguiseCommand::fromArguments)
                .registerC2S("request_meta_sync", MIC2SRequestSyncCommand::fromArguments);

        if (!prepareServer())
        {
            logger.error("Can't setup server, not enabling multi-instance service!");
            return;
        }
    }

    @Nullable
    public Runnable onStop;

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

        if (FeatherMorphMain.getInstance().debugOutputEnabled())
            logMasterInfo("WS Master :: %s :: <- :: %s".formatted(ws.getRemoteSocketAddress(), record.rawMessage()));

        try
        {
            var decode = gson.fromJson(record.rawMessage(), MIServerboundCommandRecord.class);

            var cmd = registries.createC2SCommand(decode.commandName(), decode.arguments());

            cmd.setSourceSocket(ws);
            cmd.onCommand(this);
        }
        catch (JsonSyntaxException e)
        {
            logMasterWarn("Unable to decode JSON from WebSocket '%s': %s".formatted(ws.getRemoteSocketAddress(), e.getMessage()));
            disconnect(ws, "Failed to decode JSON");
        }
        catch (Throwable t)
        {
            logMasterWarn("Error handling command from WebSocket '%s': %s".formatted(ws.getRemoteSocketAddress(), t.getMessage()));
            t.printStackTrace();

            disconnect(record.socket(), "Failed to handle client message");
        }
    }

    //region IClientHandler

    private final CommandRegistriesCopy registries = new CommandRegistriesCopy();

    private final ProtocolLevel level = ProtocolLevel.V3;

    private final Map<WebSocket, ProtocolState> allowedSockets = new Object2ObjectArrayMap<>();

    @ApiStatus.Internal
    public void broadcastCommand(MIS2CCommand command)
    {
        for (var allowedSocket : this.allowedSockets.keySet())
            this.sendCommand(allowedSocket, command);
    }

    private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    private void sendCommand(WebSocket socket, MIS2CCommand command)
    {
        if (!socket.isOpen())
        {
            logMasterWarn("Not sending commands to a closed socket! %s".formatted(socket.getRemoteSocketAddress()));
            return;
        }

        var message = gson.toJson(MIClientboundCommandRecord.fromS2CCommand(command));

        if (debug_output.get())
            logMasterInfo("WS Master :: %s :: -> :: %s".formatted(socket.getRemoteSocketAddress(), message));

        socket.send(message);
    }

    private void disconnect(WebSocket socket, String reason)
    {
        this.sendCommand(socket, new MIS2CDisconnectCommand(CloseFrame.NORMAL, reason));

        this.allowedSockets.remove(socket);
        socket.close();
    }

    private boolean socketAllowed(WebSocket socket)
    {
        return allowedSockets.getOrDefault(socket, null) != null;
    }

    private void switchState(WebSocket socket, ProtocolState state)
    {
        allowedSockets.put(socket, state);
        sendCommand(socket, new MIS2CSwitchStateCommand(state));
    }

    public ProtocolState getConnectionState(WebSocket socket)
    {
        return allowedSockets.getOrDefault(socket, ProtocolState.INVALID);
    }

    //todo: 状态切换应当分为多个函数执行
    //      现在这么做很💩️
    @Override
    public void onLoginCommand(MIC2SLoginCommand cProtocolCommand)
    {
        var socket = cProtocolCommand.getSocket();
        if (socket == null)
        {
            logger.info("Received a login request from an unknown source, not processing.");
            return;
        }

        logMasterInfo("'%s' is requesting a login".formatted(socket.getRemoteSocketAddress()));

        this.switchState(socket, ProtocolState.LOGIN);

        if (debug_output.get())
            logger.info("Level is '%s', and their secret is '%s'".formatted(cProtocolCommand.getVersion(), cProtocolCommand.getSecret()));

        if (!this.level.equals(cProtocolCommand.getVersion()))
        {
            logMasterInfo("Protocol mismatch! Disconnecting...");

            this.disconnect(socket, "Protocol mismatch!");
            return;
        }

        if (cProtocolCommand.getSecret() == null || !cProtocolCommand.getSecret().equals(this.secret.get()))
        {
            logMasterInfo("Invalid secret! Disconnecting...");

            disconnect(socket, "Invalid secret '%s'".formatted(cProtocolCommand.getSecret()));
            return;
        }

        logMasterInfo("'%s' logged in".formatted(socket.getRemoteSocketAddress()));

        sendCommand(socket, new MIS2CLoginResponseCommand(true));
        switchState(socket, ProtocolState.WAIT_LISTEN);
    }

    private final NetworkDisguiseManager disguiseManager = new NetworkDisguiseManager();

    @Override
    public void onSlaveRequestMetaSync(MIC2SRequestSyncCommand command)
    {
        var socket = command.getSocket();

        if (socket == null)
        {
            logger.info("Received a login request from an unknown source, not processing.");
            return;
        }

        var cmd = new MIS2CSyncMetaCommand();

        var disguises = disguiseManager.listAllMeta();
        for (var meta : disguises)
            cmd.appendMeta(new SocketPlayerMeta(Operation.ADD_IF_ABSENT, meta.getUnlockedDisguiseIdentifiers(), meta.uniqueId));

        this.sendCommand(socket, cmd);

        logMasterInfo("Synced %s metadata(s) to socket '%s'".formatted(disguises.size(), socket.getRemoteSocketAddress()));
    }

    @Override
    public void onDisguiseMetaCommand(MIC2SSyncDisguiseCommand cDisguiseMetaCommand)
    {
        var socket = cDisguiseMetaCommand.getSocket();
        if (!socketAllowed(socket))
            return;

        assert socket != null;

        var socketMeta = cDisguiseMetaCommand.getMeta();
        if (socketMeta == null || !socketMeta.isValid())
        {
            logMasterWarn("Bad client implementation? Got invalid meta from '%s'".formatted(socket.getRemoteSocketAddress()));
            return;
        }

        var state = getConnectionState(socket);

        if (!state.loggedIn())
        {
            logMasterWarn("Bad client implementation? They sent meta sync before they login! (%s)".formatted(socket.getRemoteSocketAddress()));
            return;
        }

        var operation = socketMeta.getOperation();
        var identifiers = socketMeta.getIdentifiers();

        var playerMeta = disguiseManager.getPlayerMeta(Bukkit.getOfflinePlayer(Objects.requireNonNull(socketMeta.getBindingUuid(), "???")));

        if (operation == Operation.ADD_IF_ABSENT)
        {
            var unlocked = playerMeta.getUnlockedDisguiseIdentifiers();
            socketMeta.getIdentifiers().forEach(str ->
            {
                if (unlocked.stream().noneMatch(s -> s.equals(str)))
                    playerMeta.addDisguise(disguiseManager.getDisguiseMeta(str));
            });

            // Broadcast to all allowed sockets
            for (var allowedSocket : this.allowedSockets.keySet())
                this.sendCommand(allowedSocket, new MIS2CUpdateMetaCommand(socketMeta));
        }
        else if (operation == Operation.REMOVE)
        {
            identifiers.forEach(id ->
            {
                var disguiseMeta = disguiseManager.getDisguiseMeta(id);

                playerMeta.removeDisguise(disguiseMeta);
            });

            // Broadcast to all allowed sockets
            for (var allowedSocket : this.allowedSockets.keySet())
                this.sendCommand(allowedSocket, new MIS2CUpdateMetaCommand(socketMeta));
        }
    }

    @Override
    public void onMessage(InstanceServer.WsRecord wsRecord, InstanceServer server)
    {
        this.addSchedule(() -> this.onText(wsRecord));
    }

    @Override
    public void onServerStart(InstanceServer server)
    {
    }

    @Override
    public void onConnectionClose(WebSocket socket)
    {
        allowedSockets.remove(socket);
    }

    //endregion

    //region Utilities

    @NotNull
    private WeakReference<SlaveInstance> slaveWeakRef = new WeakReference<>(null);

    public void setInternalSlave(SlaveInstance slave)
    {
        this.slaveWeakRef = new WeakReference<>(slave);
    }

    public void onInternalSlaveError(SlaveInstance slave, Exception e)
    {
        if (e instanceof ConnectException) return;

        logger.error("Error occurred with the internal client! Stopping master server...");
        this.stop();
    }

    public void loadInitialDisguises(List<PlayerMeta> metaList)
    {
        this.disguiseManager.merge(metaList);
    }

    //endregion
}
