package xyz.nifeather.morph.network.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import net.minecraft.network.FriendlyByteBuf;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.network.*;
import xyz.nifeather.morph.network.commands.C2S.*;
import xyz.nifeather.morph.network.commands.CommandRegistriesNew;
import xyz.nifeather.morph.network.commands.S2C.*;
import xyz.nifeather.morph.network.commands.S2C.query.QueryType;
import xyz.nifeather.morph.network.commands.S2C.query.S2CQueryCommand;
import xyz.nifeather.morph.network.commands.S2C.set.S2CSetModifyBoundingBoxCommand;
import xyz.nifeather.morph.network.commands.S2C.set.S2CSetSelfViewingStatusCommand;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.config.ConfigOption;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.interfaces.IManageRequests;
import xyz.nifeather.morph.messages.EmoteStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.MorphStrings;
import xyz.nifeather.morph.misc.ModNetworkingHelper;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.network.server.handlers.ICommandPacketHandler;
import xyz.nifeather.morph.network.server.handlers.V3ProtocolHandler;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class MorphClientHandler extends MorphPluginObject implements BasicClientHandler<Player>
{
    private final Bindable<Boolean> allowClient = new Bindable<>(false);
    private final Bindable<Boolean> logInComingPackets = new Bindable<>(false);
    private final Bindable<Boolean> logOutGoingPackets = new Bindable<>(false);
    private final Bindable<Boolean> forceTargetVersion = new Bindable<>(false);

    private final LegacyClientHandler legacyClientHandler;

    public MorphClientHandler()
    {
        legacyClientHandler = new LegacyClientHandler(this);
    }

    public boolean allowClient()
    {
        return allowClient.get();
    }

    public boolean logInComingPackets()
    {
        return logInComingPackets.get();
    }

    private final Map<Player, ICommandPacketHandler> playerCommandHandlerMap = new ConcurrentHashMap<>();

    public void setProtocolHandlerFor(Player player, ICommandPacketHandler commandPacketHandler)
    {
        playerCommandHandlerMap.put(player, commandPacketHandler);
    }

    @Nullable
    public ICommandPacketHandler getProtocolHandler(Player player)
    {
        return playerCommandHandlerMap.getOrDefault(player, null);
    }

    @NotNull
    public ICommandPacketHandler getProtocolHandlerOrThrow(Player player)
    {
        return Objects.requireNonNull(getProtocolHandler(player), "Null Protocol Handler for player '%s', is everything good?".formatted(player));
    }

    //region Send command/packet

    @Override
    public boolean sendCommand(Player player, AbstractS2CCommand<?> basicS2CCommand)
    {
        if (getSession(player) == null)
        {
            if (FeatherMorphMain.getInstance().debugOutputEnabled())
                logger.error("No Session for player " + player.getName() + ", not sending commands.");

            return false;
        }

        if (!player.isOnline() || getPlayerConnectionState(player).worseThan(InitializeState.HANDSHAKE))
            return false;

        if (!plugin.isEnabled())
            return false;

        getProtocolHandlerOrThrow(player).sendCommand(player, basicS2CCommand);
        return true;
    }

    //endregion Send command/packet

    /**
     * 服务端的接口版本
     */
    public final int targetApiVersion = Constants.PROTOCOL_VERSION;

    /**
     * 最低能接受的客户端接口版本
     */
    public final int minimumApiVersion = 1;

    @Resolved
    private MorphManager manager;

    public static void logPacket(boolean isOutGoingPacket, Player player, String channel, byte[] data)
    {
        logPacket(isOutGoingPacket, player, channel, data, false);
    }

    public static void logPacket(boolean isOutGoingPacket, Player player, String channel, byte[] data, boolean isV1Proto)
    {
        String msg;
        var input = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));

        try
        {
            msg = isV1Proto ? new String(data, StandardCharsets.UTF_8) : input.readUtf();
        }
        catch (Throwable t)
        {
            msg = "<base64> " + Base64.getEncoder().encodeToString(data);
        }

        logPacket(isOutGoingPacket, player, channel, msg, data.length);
    }

    private static void logPacket(boolean isOutGoingPacket, Player player, String channel, String data, int size)
    {
        var arrow = isOutGoingPacket ? " -> " : " <- ";

        String builder = channel + arrow
                + player.getName()
                + " :: "
                + "'%s'".formatted(data)
                + " (≈ %s bytes)".formatted(size);

        FeatherMorphMain.getInstance().getSLF4JLogger().info(builder);
    }

    private final CommandRegistriesNew registries = new CommandRegistriesNew();

    private final Bindable<Boolean> modifyBoundingBoxes = new Bindable<>(false);
    private final Bindable<Boolean> useClientRenderer = new Bindable<>(false);
    private final Bindable<Boolean> debugOutput = new Bindable<>(false);

    public static final String SERVER_FEATURE_FLAGS = "1_21_3_packetbuf";

    @Initializer
    private void load(FeatherMorphMain plugin, MorphConfigManager configManager)
    {
        registries.registerC2S(C2SCommandNames.Initial, C2SRequestInitialCommand::fromArguments)
                .registerC2S(C2SCommandNames.Morph, C2SMorphCommand::fromArguments)
                .registerC2S(C2SCommandNames.Skill, C2SActivateSkillCommand::fromArguments)
                .registerC2S(C2SCommandNames.SetSingleOption, C2SSetSingleOptionCommand::fromArguments)
                .registerC2S(C2SCommandNames.ToggleSelf, C2SToggleSelfCommand::fromArguments)
                .registerC2S(C2SCommandNames.Unmorph, C2SUnmorphCommand::fromArguments)
                .registerC2S(C2SCommandNames.Request, C2SRequestCommand::fromArguments)
                .registerC2S("animation", C2SAnimationCommand::fromArguments);

        var messenger = Bukkit.getMessenger();

        // 注册incoming频道
        messenger.registerIncomingPluginChannel(plugin, MessageChannel.initializeChannelV3, this::handleInitializeV3);
        messenger.registerOutgoingPluginChannel(plugin, MessageChannel.initializeChannelV3);

        messenger.registerIncomingPluginChannel(plugin, MessageChannel.commandChannelV3, this::handleCommandV3);
        messenger.registerOutgoingPluginChannel(plugin, MessageChannel.commandChannelV3);

        configManager.bind(allowClient, ConfigOption.ALLOW_CLIENT);
        configManager.bind(forceTargetVersion, ConfigOption.FORCE_TARGET_VERSION);

        configManager.bind(logInComingPackets, ConfigOption.LOG_INCOMING_PACKETS);
        configManager.bind(logOutGoingPackets, ConfigOption.LOG_OUTGOING_PACKETS);

        configManager.bind(modifyBoundingBoxes, ConfigOption.MODIFY_BOUNDING_BOX);

        configManager.bind(useClientRenderer, ConfigOption.USE_CLIENT_RENDERER);

        configManager.bind(debugOutput, ConfigOption.DEBUG_OUTPUT);

        modifyBoundingBoxes.onValueChanged((o, n) ->
        {
            var players = Bukkit.getOnlinePlayers();
            players.forEach(p -> sendCommand(p, new S2CSetModifyBoundingBoxCommand(n)));
        });

        forceTargetVersion.onValueChanged((o, n) -> scheduleReAuthPlayers());
        modifyBoundingBoxes.onValueChanged((o, n) -> scheduleReAuthPlayers());
        useClientRenderer.onValueChanged((o, n) -> scheduleReAuthPlayers());

        allowClient.onValueChanged((o, n) ->
        {
            var players = Bukkit.getOnlinePlayers();

            if (n)
                players.forEach(this::disconnectThenReAuth);
            else
                players.forEach(this::disconnect);
        });

        Bukkit.getOnlinePlayers().forEach(p ->
        {
            var session = this.getOrCreateSession(p);
            session.connectionState = ConnectionState.JOINED;
        });
    }

    private final AtomicBoolean scheduledReauthPlayers = new AtomicBoolean(false);

    //region Handle Protocol Inputs

    public InitializeRespondV3 getInitializeRespond()
    {
        return new InitializeRespondV3(List.of(SERVER_FEATURE_FLAGS), this.targetApiVersion);
    }

    private void handleInitializeV3(@NotNull String channel, @NotNull Player player, byte @NotNull [] rawData)
    {
        logPacket(false, player, channel, rawData);

        var handleResult = V3ProtocolHandler.V3_INSTANCE.handleInitializeData(player, rawData);
        if (!handleResult.handleSuccess())
        {
            rejectPlayer(player);
            return;
        }

        this.setProtocolHandlerFor(player, V3ProtocolHandler.V3_INSTANCE);

        logger.info("%s is using V3 packets".formatted(player.getName()));
        this.handleHandshakeMessage(V3ProtocolHandler.V3_INSTANCE, player, handleResult);
    }

    public void handleHandshakeMessage(ICommandPacketHandler commandPacketHandler, @NotNull Player player, @NotNull ClientInitializeRecordV3 clientInitializeRecord)
    {
        if (!allowClient.get() || this.getPlayerConnectionState(player).greaterThan(InitializeState.HANDSHAKE)) return;

        // This is BAD!
        // We should find another better way to make sure we always send commands when the channel is added.
        ((CraftPlayer) player).addChannel(MessageChannel.commandChannelV3);
        ((CraftPlayer) player).addChannel(MessageChannel.initializeChannelV3);

        int clientVersion = clientInitializeRecord.apiVersion();

        var minimumApiVersion = this.minimumApiVersion;

        if (forceTargetVersion.get()) minimumApiVersion = targetApiVersion;

        //如果客户端版本低于最低能接受的版本或高于当前版本，拒绝初始化
        if (clientVersion < minimumApiVersion || clientVersion > Constants.PROTOCOL_VERSION)
        {
            disconnect(player);

            //player.sendMessage(MessageUtils.prefixes(player, MorphStrings.clientVersionMismatchString()));
            logger.info(player.getName() + " joined with incompatible client API version: " + clientVersion + " (This server requires " + targetApiVersion + ")");

            var msg = forceTargetVersion.get() ? MorphStrings.clientVersionMismatchKickString() : MorphStrings.clientVersionMismatchString();
            msg.withLocale(MessageUtils.getLocale(player))
                    .resolve("minimum_version", Component.text(minimumApiVersion))
                    .resolve("player_version", Component.text(clientVersion));

            if (forceTargetVersion.get())
                player.kick(msg.toComponent());
            else
                player.sendMessage(msg.toComponent());

            return;
        }

        logger.info(player.getName() + " joined with API version " + clientVersion);

        var session = getOrCreateSession(player);
        session.options.clientApiVersion = clientInitializeRecord.apiVersion();
        session.initializeState = InitializeState.API_CHECKED;

        commandPacketHandler.sendInitializeRespond(player, this.getInitializeRespond());
    }

    private void handleCommandV3(@NotNull String channel, @NotNull Player player, byte @NotNull [] data)
    {
        logPacket(false, player, channel, data);
        handleCommandFromHandlerInternal(V3ProtocolHandler.V3_INSTANCE, channel, player, data);
    }

    @ApiStatus.Internal
    public void handleCommandFromHandlerInternal(ICommandPacketHandler protocolHandler, @NotNull String channel, @NotNull Player player, byte @NotNull [] data)
    {
        if (logInComingPackets.get())
            logPacket(false, player, channel, data);

        var result = protocolHandler.handleCommandData(player, data);

        if (!result.success())
        {
            logger.info("Failed to decode command from player '%s', rejecting...".formatted(player.getName()));
            rejectPlayer(player);
            return;
        }

        this.handleCommandInput(player, result.result());
    }

    public void handleCommandInput(Player player, C2SCommandRecord commandRecord)
    {
        if (!allowClient.get()) return;

        var session = getSession(player);
        if (session == null || session.initializeState.worseThan(InitializeState.API_CHECKED)) return;

        AbstractC2SCommand<?> command;

        try
        {
            command = registries.createC2SCommand(commandRecord.commandName(), commandRecord.arguments());
        }
        catch (Throwable t)
        {
            logger.warn("Failed to create command instance from '%s': %s".formatted(player, t.getMessage()));
            rejectPlayer(player);
            return;
        }

        command.setOwner(player);
        command.onCommand(this);
    }

    //endregion Handle Protocol Inputs

    private final Map<Player, PlayerSession> playerSessionMap = new ConcurrentHashMap<>();

    private PlayerSession createSession(Player player)
    {
        var cached = playerSessionMap.getOrDefault(player, null);

        if (cached != null)
            return cached;

        var instance = PlayerSession.SessionBuilder
                .builder(player)
                .isLegacy(false)
                .build();

        playerSessionMap.put(player, instance);
        return instance;
    }

    @Nullable
    public PlayerSession getSession(Player player)
    {
        return playerSessionMap.getOrDefault(player, null);
    }

    @NotNull
    public PlayerSession getOrCreateSession(Player player)
    {
        return createSession(player);
    }

    //region wait until ready

    public void markPlayerJoined(Player player)
    {
        var session = getOrCreateSession(player);
        session.connectionState = ConnectionState.JOINED;
    }

    //endregion

    /**
     * 刷新某个玩家的客户端的伪装列表
     *
     * @param identifiers 伪装列表
     * @param player 目标玩家
     */
    public void refreshPlayerClientMorphs(List<String> identifiers, Player player)
    {
        if (!allowClient.get()) return;

        this.sendCommand(player, new S2CQueryCommand(QueryType.SET, identifiers));
    }

    /**
     * 向某个玩家的客户端发送差异信息
     *
     * @param addits 添加
     * @param removal 删除
     * @param player 目标玩家
     */
    public void sendDiff(@Nullable List<String> addits, @Nullable List<String> removal, Player player)
    {
        if (!allowClient.get()) return;

        if (addits != null)
            this.sendCommand(player, new S2CQueryCommand(QueryType.ADD, addits));

        if (removal != null)
            this.sendCommand(player, new S2CQueryCommand(QueryType.REMOVE, removal));
    }

    /**
     * 更新某一玩家客户端的当前伪装
     *
     * @param player 目标玩家
     * @param str 伪装ID
     */
    public void updateCurrentIdentifier(Player player, String str)
    {
        if (!allowClient.get()) return;

        this.sendCommand(player, new S2CCurrentCommand(str));
    }

    //region Auth/UnAuth/ReAuth

    private void scheduleReAuthPlayers()
    {
        synchronized (scheduledReauthPlayers)
        {
            if (scheduledReauthPlayers.get()) return;
            scheduledReauthPlayers.set(true);
        }

        this.addSchedule(() ->
        {
            synchronized (scheduledReauthPlayers)
            {
                if (!scheduledReauthPlayers.get()) return;

                scheduledReauthPlayers.set(false);
                reAuthPlayers(Bukkit.getOnlinePlayers());
            }
        });
    }


    public void rejectPlayer(Player player)
    {
        logger.info("Rejecting player " + player.getName());
        player.sendMessage(MessageUtils.prefixes(player, MorphStrings.unsupportedClientBehavior()));

        this.disconnect(player);
    }

    public void disconnectThenReAuth(Player player)
    {
        var handler = getProtocolHandler(player);
        if (handler == null)
            return;

        disconnect(player);
        handler.sendInitializeRespond(player, getInitializeRespond());
    }

    /**
     * 向列表中的玩家客户端发送reauth指令
     *
     * @param players 玩家列表
     */
    public void reAuthPlayers(Collection<? extends Player> players)
    {
        if (!allowClient.get()) return;

        players.forEach(this::disconnectThenReAuth);
    }

    //endregion Auth/UnAuth

    //region Player Status/Properties/Option

    public boolean isFutureClientProtocol(Player player, int version)
    {
        return getPlayerVersion(player) >= version;
    }

    /**
     * 获取玩家的连接状态
     *
     * @param player 目标玩家
     * @return {@link InitializeState}, 客户端未连接或初始化被中断时返回 {@link InitializeState#NOT_CONNECTED}
     */
    public InitializeState getPlayerConnectionState(Player player)
    {
        var session = this.getSession(player);
        if (session == null) return InitializeState.NOT_CONNECTED;

        return session.initializeState;
    }

    /**
     * 检查某个玩家是否使用客户端加入
     *
     * @param player 目标玩家
     * @return 玩家是否使用客户端加入
     * @apiNote 此API只能检查客户端是否已连接，检查初始化状态请使用 {@link MorphClientHandler#clientInitialized(Player)}
     */
    public boolean clientConnected(Player player)
    {
        return this.getPlayerConnectionState(player).greaterThan(InitializeState.NOT_CONNECTED);
    }

    /**
     * 检查某个玩家的客户端是否已初始化
     *
     * @param player 目标玩家
     * @return 此玩家的客户端是否已初始化
     */
    public boolean clientInitialized(Player player)
    {
        var session = this.getSession(player);
        if (session == null) return false;

        return session.initializeState == InitializeState.DONE;
    }

    @Nullable
    @Contract("_, false -> null; _, true -> !null")
    public PlayerOptions<Player> getPlayerOption(Player player, boolean createSessionIfNull)
    {
        var session = getSession(player);

        if (session != null)
            return session.options;
        else if (!createSessionIfNull)
            return null;

        return createSession(player).options;
    }

    /**
     * 获取某一玩家的客户端选项
     * @param player 目标玩家
     * @return 此玩家的客户端选项
     */
    @Nullable
    public PlayerOptions<Player> getPlayerOption(Player player)
    {
        var session = getSession(player);
        if (session == null) return null;

        return session.options;
    }

    @Override
    public int getPlayerVersion(Player player)
    {
        var option = getPlayerOption(player);

        return option == null ? -1 : option.clientApiVersion;
    }

    @Override
    public InitializeState getInitializeState(Player player)
    {
        var session = getSession(player);

        return session == null ? InitializeState.NOT_CONNECTED : session.initializeState;
    }

    @Override
    public boolean isPlayerInitialized(Player player)
    {
        return getInitializeState(player) == InitializeState.DONE;
    }

    @Override
    public boolean isPlayerConnected(Player player)
    {
        return getInitializeState(player).greaterThan(InitializeState.PENDING);
    }

    @Override
    public List<Player> getConnectedPlayers()
    {
        return playerSessionMap.keySet().stream().toList();
    }

    //endregion Player Status/Option

    @Override
    public void disconnect(Player player)
    {
        if (!this.playerSessionMap.containsKey(player))
        {
            if (FeatherMorphMain.getInstance().doInternalDebugOutput)
                logger.info("Skipping disconnect for player %s since it does not have a session.".formatted(player));

            return;
        }

        this.sendCommand(player, new S2CUnAuthCommand());

        this.playerSessionMap.remove(player);

        var playerConfig = manager.getPlayerMeta(player);

        var state = manager.getDisguiseStateFor(player);
        if (state != null)
            state.setServerSideSelfVisible(playerConfig.showDisguiseToSelf);

        this.playerCommandHandlerMap.remove(player);
    }

    //region C2S(Serverbound) commands

    @Override
    public void onInitialCommand(C2SRequestInitialCommand c2SInitialCommand)
    {
        Player player = c2SInitialCommand.getOwner();

        if (this.clientInitialized(player)) return;

        var session = getOrCreateSession(player);
        if (session.connectionState != ConnectionState.JOINED)
            session.connectionState = ConnectionState.CONNECTING;

        //再检查一遍玩家有没有初始化完成
        if (clientInitialized(player))
            return;

        var config = manager.getPlayerMeta(player);
        var list = config.getUnlockedDisguiseIdentifiers();
        refreshPlayerClientMorphs(list, player);

        var state = manager.getDisguiseStateFor(player);

        if (state != null)
            manager.refreshClientState(state);

        sendCommand(player, new S2CSetSelfViewingStatusCommand(config.showDisguiseToSelf));
        sendCommand(player, new S2CSetModifyBoundingBoxCommand(modifyBoundingBoxes.get()));

        if (player.hasPermission(CommonPermissions.DISGUISE_REVEALING))
            sendCommand(player, manager.genMapCommand());

        if (state != null)
            state.getDisguiseWrapper().getBackend().onClientModInitialize(player, this, manager);

        session.initializeState = InitializeState.DONE;
    }

    @Override
    public void onMorphCommand(C2SMorphCommand c2SMorphCommand)
    {
        Player player = c2SMorphCommand.getOwner();
        var id = c2SMorphCommand.identifier();

        if (id.isBlank())
            manager.tryQuickDisguise(player);
        else if (manager.canMorph(player))
            manager.morph(player, player, id, player.getTargetEntity(5));
    }

    @Override
    public void onOptionCommand(C2SSetSingleOptionCommand c2SOptionCommand)
    {
        var option = c2SOptionCommand.getOption();
        Player player = c2SOptionCommand.getOwner();

        switch (option)
        {
            case CLIENTVIEW ->
            {
                var val = Boolean.parseBoolean(c2SOptionCommand.getValue());
                this.getPlayerOption(player, true).setClientSideSelfView(val);

                var state = manager.getDisguiseStateFor(player);
                if (state != null) state.setServerSideSelfVisible(!val);
            }

            case HUD ->
            {
                var val = Boolean.parseBoolean(c2SOptionCommand.getValue());
                this.getPlayerOption(player, true).displayDisguiseOnHUD = val;

                if (!val) player.sendActionBar(Component.empty());
            }
        }
    }

    @Override
    public void onSkillCommand(C2SActivateSkillCommand c2SSkillCommand)
    {
        manager.executeDisguiseSkill(c2SSkillCommand.getOwner());
    }

    @Override
    public void onToggleSelfCommand(C2SToggleSelfCommand c2SToggleSelfCommand)
    {
        Player player = c2SToggleSelfCommand.getOwner();

        var playerOption = this.getPlayerOption(player, true);
        var playerConfig = manager.getPlayerMeta(player);

        switch (c2SToggleSelfCommand.getSelfViewMode())
        {
            case ON ->
            {
                if (playerConfig.showDisguiseToSelf) return;
                manager.setSelfDisguiseVisible(player, true, true, false, false);
            }

            case OFF ->
            {
                if (!playerConfig.showDisguiseToSelf) return;
                manager.setSelfDisguiseVisible(player, false, true, false, false);
            }

            case CLIENT_ON ->
            {
                playerOption.setClientSideSelfView(true);

                var state = manager.getDisguiseStateFor(player);

                if (state != null)
                    state.setServerSideSelfVisible(false);
            }

            case CLIENT_OFF ->
            {
                playerOption.setClientSideSelfView(false);

                var state = manager.getDisguiseStateFor(player);

                if (state != null)
                    state.setServerSideSelfVisible(true);
            }
        }
    }

    @Override
    public void onUnmorphCommand(C2SUnmorphCommand c2SUnmorphCommand)
    {
        manager.unMorph(c2SUnmorphCommand.getOwner());
    }

    @Resolved
    private IManageRequests requestManager;

    @Resolved
    private ModNetworkingHelper modNetworkingHelper;

    @Override
    public void onRequestCommand(C2SRequestCommand c2SRequestCommand)
    {
        Player player = c2SRequestCommand.getOwner();
        var target = c2SRequestCommand.targetRequestName;
        var deceison = c2SRequestCommand.decision;

        if (target.equalsIgnoreCase("unknown") || deceison == C2SRequestCommand.Decision.UNKNOWN)
        {
            logger.warn("Received an invalid request response");
            return;
        }

        var targetPlayer = Bukkit.getPlayerExact(target);
        if (targetPlayer == null) return;

        if (deceison == C2SRequestCommand.Decision.ACCEPT)
            requestManager.acceptRequest(player, targetPlayer);
        else
            requestManager.denyRequest(player, targetPlayer);
    }

    @Override
    public void onAnimationCommand(C2SAnimationCommand c2SAnimationCommand)
    {
        var player = (Player) c2SAnimationCommand.getOwner();
        var state = manager.getDisguiseStateFor(player);
        if (state == null) return;

        var animationProvider = state.getProvider().getAnimationProvider();
        var disguiseID = state.getDisguiseIdentifier();
        var animationID = c2SAnimationCommand.getAnimationId();
        var sequencePair =  animationProvider.getAnimationSetFor(disguiseID).sequenceOf(animationID);

        if (!state.tryScheduleSequence(animationID, sequencePair.left(), sequencePair.right()))
            player.sendMessage(MessageUtils.prefixes(player, EmoteStrings.notAvailable()));
    }

    //endregion C2S(Serverbound) commands
}
