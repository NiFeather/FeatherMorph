package xyz.nifeather.morph.network.multiInstance.slave;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.java_websocket.framing.CloseFrame;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.config.ConfigOption;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.network.multiInstance.IInstanceService;
import xyz.nifeather.morph.network.multiInstance.protocol.*;
import xyz.nifeather.morph.network.multiInstance.protocol.c2s.MIC2SCommand;
import xyz.nifeather.morph.network.multiInstance.protocol.c2s.MIC2SLoginCommand;
import xyz.nifeather.morph.network.multiInstance.protocol.c2s.MIC2SRequestSyncCommand;
import xyz.nifeather.morph.network.multiInstance.protocol.s2c.*;
import xyz.nifeather.morph.network.server.MorphClientHandler;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SlaveInstance extends MorphPluginObject implements IInstanceService, IMasterHandler
{
    @Nullable
    private InstanceClient client;
    private volatile CompletableFuture<Void> clientRunningFuture;

    public SlaveInstance(boolean startOnLoad)
    {
        super();

        this.startOnLoad = startOnLoad;
        this.playerDataHolder = new NetworkDataHolder(this);

        load();
    }

    public void resetDataStore()
    {
        if (Objects.equals(morphManager.getDataStore(), this.playerDataHolder))
            morphManager.setDataStore(null);
    }

    private boolean stopClient()
    {
        if (client == null) return true;

        try
        {
            client.close(CloseFrame.GOING_AWAY, "noRetry");
            client.dispose();
            client = null;

            this.clientRunningFuture.cancel(true);

            return true;
        }
        catch (Throwable t)
        {
            logSlaveWarn("Can't close client! " + t.getMessage());
            t.printStackTrace();
            return false;
        }
    }

    private void logSlaveInfo(String message)
    {
        logger.info("[Slave@%s] %s".formatted(Integer.toHexString(this.hashCode()), message));
    }

    private void logSlaveWarn(String message)
    {
        logger.warn("[Slave@%s] %s".formatted(Integer.toHexString(this.hashCode()), message));
    }

    /**
     * @return Whether this operation operates successfully
     */
    private boolean prepareClient()
    {
        if (!stopClient())
            return false;

        try
        {
            var rawAddr = config.getOrDefault(String.class, ConfigOption.MASTER_ADDRESS);
            var uri = URI.create("ws://" + rawAddr);

            var client = new InstanceClient(uri, plugin, this);

            this.client = client;

            //todo: Use a Thread to run client
            this.clientRunningFuture = CompletableFuture.runAsync(client);

            return true;
        }
        catch (Throwable t)
        {
            logSlaveWarn("Error occurred setting up client: " + t.getMessage());
            t.printStackTrace();

            return false;
        }
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphConfigManager config;

    private final boolean startOnLoad;

    private final NetworkDataHolder playerDataHolder;

    private void load()
    {
        logSlaveInfo("Preparing multi-instance client...");

        config.bind(secret, ConfigOption.MASTER_SECRET);

        registries.registerS2C("deny", MIS2CDisconnectCommand::fromArguments)
                .registerS2C("dmeta", MIS2CUpdateMetaCommand::fromArguments)
                .registerS2C("r_login", MIS2CLoginResponseCommand::fromArguments)
                .registerS2C("state", MIS2CSwitchStateCommand::fromArguments)
                .registerS2C("sync_player_meta", MIS2CSyncMetaCommand::fromArguments);

        if (client != null) return;

        if (!startOnLoad) return;

        if (prepareClient())
            morphManager.setDataStore(new VoidDataHolder());
        else
            logSlaveWarn("Can't setup client, this instance will stay offline from the instance network!");
    }

    private final Bindable<String> secret = new Bindable<>(null);

    @Override
    public boolean stop()
    {
        return stopClient();
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphManager morphManager;

    @Resolved(shouldSolveImmediately = true)
    private MorphClientHandler clientHandler;

    private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @ApiStatus.Internal
    public void sendCommand(MIC2SCommand command)
    {
        if (client == null)
            throw new NullDependencyException("Null client!");

        var cmd = gson.toJson(MIServerboundCommandRecord.fromC2SCommand(command));

        if (client.isClosed())
        {
            if (FeatherMorphMain.getInstance().debugOutputEnabled())
                logSlaveInfo("[debug] We are sending WebSocket request while being offline?! trying to send '%s'".formatted(cmd));

            return;
        }

        if (FeatherMorphMain.getInstance().debugOutputEnabled())
            logSlaveInfo("WS Slave :: -> SERVER :: " + cmd);

        client.send(cmd);
    }

    @Nullable
    private volatile CompletableFuture<SlaveInstance> dataSyncFuture;

    public CompletableFuture<SlaveInstance> requestDataSync()
    {
        if (dataSyncFuture != null)
            return dataSyncFuture;

        if (FeatherMorphMain.getInstance().debugOutputEnabled())
            logSlaveInfo("Requesting data sync...");

        this.sendCommand(new MIC2SRequestSyncCommand());

        return new CompletableFuture<>();
    }

    public boolean isOnline()
    {
        return client != null && client.isOpen();
    }

    @Override
    public void onSyncMeta(MIS2CSyncMetaCommand command)
    {
        logSlaveInfo("Received data sync for %s entries".formatted(command.data().size()));

        playerDataHolder.dropAll();
        for (SocketPlayerMeta socketMeta : command.data())
        {
            if (!socketMeta.isValid())
                continue;

            var offlinePlayer = Bukkit.getOfflinePlayer(Objects.requireNonNull(socketMeta.getBindingUuid(), "???"));
            var playerMeta = playerDataHolder.getPlayerMeta(offlinePlayer);

            for (var identifier : socketMeta.getIdentifiers())
            {
                var disguiseMeta = playerDataHolder.getDisguiseMeta(identifier);
                if (disguiseMeta == null)
                    continue;

                playerMeta.addDisguise(disguiseMeta);
            }
        }

        morphManager.refreshDisguiseUnlockStateToAllPlayers();
    }

    @Override
    public void onUpdateMetaCommand(MIS2CUpdateMetaCommand metaCommand)
    {
        if (!currentState.get().loggedIn())
        {
            logSlaveWarn("Bad server implementation? They are trying to sync meta before we login!");
            return;
        }

        var socketMeta = metaCommand.getMeta();
        if (!socketMeta.isValid())
        {
            logSlaveWarn("Bad server implementation? The meta is invalid!");
            return;
        }

        this.onReceivePlayerMeta(socketMeta);
    }

    private void onReceivePlayerMeta(SocketPlayerMeta socketMeta)
    {
        var operation = socketMeta.getOperation();

        var offlinePlayer = Bukkit.getOfflinePlayer(Objects.requireNonNull(socketMeta.getBindingUuid(), "???"));
        var player = offlinePlayer.getPlayer();
        var playerMeta = morphManager.getPlayerMeta(offlinePlayer);

        if (operation == Operation.ADD_IF_ABSENT)
        {
            var unlocked = playerMeta.getUnlockedDisguiseIdentifiers();

            List<String> diff = new ObjectArrayList<>();
            socketMeta.getIdentifiers().forEach(str ->
            {
                if (unlocked.contains(str))
                    return;

                diff.add(str);
                playerMeta.addDisguise(morphManager.getDisguiseMeta(str));
            });

            if (player != null && !diff.isEmpty())
            {
                if (diff.size() < 5)
                    clientHandler.sendDiff(diff, null, player);
                else
                    clientHandler.refreshPlayerClientMorphs(playerMeta.getUnlockedDisguiseIdentifiers(), player);
            }
        }
        else if (operation == Operation.REMOVE)
        {
            List<String> diff = new ObjectArrayList<>();
            socketMeta.getIdentifiers().forEach(id ->
            {
                var disguiseMeta = morphManager.getDisguiseMeta(id);

                playerMeta.removeDisguise(disguiseMeta);
            });

            if (player != null && !diff.isEmpty())
            {
                if (diff.size() < 5)
                    clientHandler.sendDiff(null, diff, player);
                else
                    clientHandler.refreshPlayerClientMorphs(playerMeta.getUnlockedDisguiseIdentifiers(), player);
            }
        }
    }

    @Override
    public void onDisconnectCommand(MIS2CDisconnectCommand cDenyCommand)
    {
        this.stopClient();
    }

    @Override
    public void onLoginResponse(MIS2CLoginResponseCommand cLoginResultCommand)
    {
        if (currentState.get() != ProtocolState.LOGIN)
        {
            logSlaveWarn("Bad server implementation? They sent a login result at when we are not in a login process!");
            return;
        }

        if (!cLoginResultCommand.isAllowed())
        {
            logSlaveWarn("Server refused the login");
            return;
        }

        logSlaveInfo("Done logging in! Setting up data holder");
        morphManager.setDataStore(this.playerDataHolder);
    }

    private final Bindable<ProtocolState> currentState = new Bindable<>(ProtocolState.NOT_CONNECTED);

    @Override
    public void onStateCommand(MIS2CSwitchStateCommand cStateCommand)
    {
        if (cStateCommand.getState() == ProtocolState.INVALID)
            logSlaveWarn("Bad server implementation? The new session state is invalid!");

        switchState(cStateCommand.getState());
    }

    private void switchState(ProtocolState newState)
    {
        logSlaveInfo("Client networking state switched to " + newState);
        currentState.set(newState);
    }

    private final ProtocolLevel implementingLevel = ProtocolLevel.V3;

    @Override
    public void onConnectionOpen()
    {
        this.addSchedule(() -> this.sendCommand(new MIC2SLoginCommand(implementingLevel, secret.get())));
    }

    @Nullable
    public Consumer<Integer> onClose;

    @Override
    public void onConnectionClose(int code)
    {
        if (onClose != null)
            onClose.accept(code);
    }

    @Override
    public void onClientError(Exception e, InstanceClient client)
    {
    }

    private final CommandRegistriesCopy registries = new CommandRegistriesCopy();

    @Override
    public void onText(String text)
    {
        this.addSchedule(() -> onCommandRaw(text));
    }

    private void onCommandRaw(String raw)
    {
        try
        {
            var decode = gson.fromJson(raw, MIClientboundCommandRecord.class);
            var cmd = registries.createS2CCommand(decode.commandName(), decode.arguments());

            cmd.onCommand(this);
        }
        catch (Throwable t)
        {
            logSlaveWarn("Failed to handle message from master instance, stopping! (%s)".formatted(t.getMessage()));
            stopClient();
        }
    }
}
