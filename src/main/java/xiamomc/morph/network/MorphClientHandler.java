package xiamomc.morph.network;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.MessageTooLargeException;
import org.bukkit.plugin.messaging.Messenger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.morph.network.commands.C2S.*;
import xiamomc.morph.network.commands.S2C.*;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MorphClientHandler extends MorphPluginObject
{
    private final Bindable<Boolean> allowClient = new Bindable<>(false);
    private final Bindable<Boolean> logInComingPackets = new Bindable<>(false);
    private final Bindable<Boolean> logOutGoingPackets = new Bindable<>(false);

    //部分来自 CraftPlayer#sendPluginMessage(), 在我们搞清楚到底为什么服务端会吞包之前先这样
    private void sendPacket(String channel, Player player, byte[] message)
    {
        if (channel == null || player == null || message == null)
            throw new IllegalArgumentException("频道、玩家或消息是null");

        if (message.length > Messenger.MAX_MESSAGE_SIZE)
            throw new MessageTooLargeException();

        if (logOutGoingPackets.get())
            logger.info(channel + " :: " + player.getName() + " -> " + new String(message, StandardCharsets.UTF_8));

        var nmsPlayer = ((CraftPlayer) player).getHandle();

        var packet = new ClientboundCustomPayloadPacket(new ResourceLocation(channel), new FriendlyByteBuf(Unpooled.wrappedBuffer(message)));
        nmsPlayer.connection.send(packet);
    }

    /**
     * 服务端的接口版本
     */
    public final int targetApiVersion = 3;

    /**
     * 最低能接受的客户端接口版本
     */
    public final int minimumApiVersion = 1;

    @Resolved
    private MorphManager manager;

    private final List<AbstractC2SCommand> c2SCommands = new ObjectArrayList<>();

    @Initializer
    private void load(MorphPlugin plugin, MorphConfigManager configManager)
    {
        Collections.addAll(c2SCommands,
                new C2SInitialCommand(playerStateMap, playerConnectionStates),
                new C2SMorphCommand(),
                new C2SOptionCommand(),
                new C2SSkillCommand(),
                new C2SToggleSelfCommand(),
                new C2SUnmorphCommand());

        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, initializeChannel, (cN, player, data) ->
        {
            if (!allowClient.get() || this.getPlayerConnectionState(player).greaterThan(InitializeState.HANDSHAKE)) return;

            if (logInComingPackets.get())
                logger.info("收到了来自" + player.getName() + "的初始化消息：" + new String(data, StandardCharsets.UTF_8));

            this.sendPacket(initializeChannel, player, "".getBytes());

            playerConnectionStates.put(player, InitializeState.HANDSHAKE);
        });

        var apiVersionBytes = ByteBuffer.allocate(4).putInt(targetApiVersion).array();
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, versionChannel, (cN, player, data) ->
        {
            if (!allowClient.get()) return;

            var connectionState = this.getPlayerConnectionState(player);

            //初始化之前忽略API请求
            if (connectionState.worseThan(InitializeState.HANDSHAKE)) return;

            var str = new String(data, StandardCharsets.UTF_8);

            if (logInComingPackets.get())
                logger.info("收到了来自" + player.getName() + "的API请求：" + str);

            //尝试获取api版本
            int clientVersion = 1;

            try
            {
                clientVersion = Integer.parseInt(str);
            }
            catch (Throwable ignored)
            {
            }

            //如果客户端版本低于最低能接受的版本，拒绝初始化
            if (clientVersion < minimumApiVersion)
            {
                unInitializePlayer(player);

                player.sendMessage(MessageUtils.prefixes(player, MorphStrings.clientVersionMismatchString()));
                logger.info(player.getName() + "使用了不支持的客户端版本：" + clientVersion + "(此服务器要求至少为" + targetApiVersion + ")");
                return;
            }

            logger.info(player.getName() + "的客户端版本是" + clientVersion);

            this.getPlayerOption(player).clientApiVersion = clientVersion;
            playerConnectionStates.put(player, InitializeState.API_CHECKED);

            this.sendPacket(versionChannel, player, apiVersionBytes);
        });

        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, commandChannel, (cN, player, data) ->
        {
            if (!allowClient.get()) return;

            //在API检查完成之前忽略客户端的所有指令
            if (this.getPlayerConnectionState(player).worseThan(InitializeState.API_CHECKED)) return;

            if (logInComingPackets.get())
                logger.info("在" + cN + "收到了来自" + player.getName() + "的服务端指令：" + new String(data, StandardCharsets.UTF_8));

            var str = new String(data, StandardCharsets.UTF_8).split(" ", 2);

            if (str.length < 1) return;

            var baseCommand = str[0];
            var c2sCommand = c2SCommands.stream()
                    .filter(c -> c.getBaseName().equals(baseCommand))
                    .findFirst().orElse(null);

            if (c2sCommand != null)
                c2sCommand.onCommand(player, str);
            else
                logger.warn("未知的服务端指令：" + baseCommand);
        });

        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, initializeChannel);
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, versionChannel);
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, commandChannel);

        configManager.bind(allowClient, ConfigOption.ALLOW_CLIENT);
        configManager.bind(logInComingPackets, ConfigOption.LOG_INCOMING_PACKETS);
        configManager.bind(logOutGoingPackets, ConfigOption.LOG_OUTGOING_PACKETS);

        allowClient.onValueChanged((o, n) ->
        {
            var players = Bukkit.getOnlinePlayers();
            players.forEach(this::unInitializePlayer);

            if (n)
                this.sendReAuth(players);
            else
                this.sendUnAuth(players);
        });

        Bukkit.getOnlinePlayers().forEach(p -> playerStateMap.put(p, ConnectionState.JOINED));
    }

    //region wait until ready
    @ApiStatus.Internal
    public void waitUntilReady(Player player, Runnable r)
    {
        var bool = playerStateMap.getOrDefault(player, null);

        if (bool == null)
        {
            //logger.info("should remove for " + player.getName());
            return;
        }

        if (bool == ConnectionState.JOINED)
        {
            r.run();
        }
        else
        {
            //logger.info(player.getName() + " not ready! " + bool);
            this.addSchedule(() -> waitUntilReady(player, r));
        }
    }

    private final Map<Player, ConnectionState> playerStateMap = new Object2ObjectOpenHashMap<>();

    public void markPlayerReady(Player player)
    {
        playerStateMap.put(player, ConnectionState.JOINED);
    }
    //endregion

    private final Map<UUID, MorphClientOptions> playerOptionMap = new Object2ObjectOpenHashMap<>();

    /**
     * 获取某一玩家的客户端选项
     * @param player 目标玩家
     * @return 此玩家的客户端选项
     */
    public MorphClientOptions getPlayerOption(Player player)
    {
        var uuid = player.getUniqueId();
        var option = playerOptionMap.getOrDefault(uuid, null);

        if (option != null) return option;

        option = new MorphClientOptions(player);
        playerOptionMap.put(uuid, option);

        return option;
    }

    public boolean clientVersionCheck(Player player, int version)
    {
        return this.getPlayerOption(player).clientApiVersion >= version;
    }

    private final Map<Player, InitializeState> playerConnectionStates = new Object2ObjectOpenHashMap<>();

    public List<Player> getClientPlayers()
    {
        return new ObjectArrayList<>(playerConnectionStates.keySet());
    }

    /**
     * 获取玩家的连接状态
     *
     * @param player 目标玩家
     * @return {@link InitializeState}, 客户端未连接或初始化被中断时返回 {@link InitializeState#NOT_CONNECTED}
     */
    public InitializeState getPlayerConnectionState(Player player)
    {
        return playerConnectionStates.getOrDefault(player, InitializeState.NOT_CONNECTED);
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
        return playerConnectionStates.getOrDefault(player, null) == InitializeState.DONE;
    }

    /**
     * 刷新某个玩家的客户端的伪装列表
     *
     * @param identifiers 伪装列表
     * @param player 目标玩家
     */
    public void refreshPlayerClientMorphs(List<String> identifiers, Player player)
    {
        if (!allowClient.get()) return;

        sendClientCommand(player, new S2CQuerySetCommand(identifiers.toArray(new String[]{})));
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
            sendClientCommand(player, new S2CQueryAddCommand(addits.toArray(new String[]{})));

        if (removal != null)
            sendClientCommand(player, new S2CQueryRemoveCommand(removal.toArray(new String[]{})));
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

        sendClientCommand(player, new S2CCurrentCommand(str));
    }

    /**
     * 反初始化玩家
     *
     * @param player 目标玩家
     */
    public void unInitializePlayer(Player player)
    {
        playerOptionMap.remove(player.getUniqueId());
        playerStateMap.remove(player);
        playerConnectionStates.remove(player);

        var playerConfig = manager.getPlayerConfiguration(player);
        var state = manager.getDisguiseStateFor(player);
        if (state != null) state.setServerSideSelfVisible(playerConfig.showDisguiseToSelf);
    }

    /**
     * 向列表中的玩家客户端发送reauth指令
     *
     * @param players 玩家列表
     */
    public void sendReAuth(Collection<? extends Player> players)
    {
        if (!allowClient.get()) return;

        players.forEach(p ->
        {
            playerStateMap.put(p, ConnectionState.JOINED);
            sendClientCommand(p, new S2CReAuthCommand());
        });
    }

    /**
     * 向列表中的玩家客户端发送unauth指令
     *
     * @param players 玩家列表
     */
    public void sendUnAuth(Collection<? extends Player> players)
    {
        players.forEach(p ->
        {
            sendClientCommand(p, new S2CUnAuthCommand(), true);
            unInitializePlayer(p);
        });
    }

    private void sendClientCommand(Player player, AbstractS2CCommand<?> command, boolean overrideClientSetting)
    {
        var cmd = command.buildCommand();
        if (cmd == null || cmd.isEmpty() || cmd.isBlank()) return;

        if (!allowClient.get() && !overrideClientSetting) return;

        this.sendPacket(commandChannel, player, cmd.getBytes());
    }

    /**
     * 向某一玩家的客户端发送指令
     *
     * @param player 目标玩家
     * @param command 要发送的指令
     */
    public <T> void sendClientCommand(Player player, AbstractS2CCommand<T> command)
    {
        this.sendClientCommand(player, command, false);
    }

    private static final String nameSpace = MorphPlugin.getMorphNameSpace();

    public static final String initializeChannel = nameSpace + ":init";
    public static final String versionChannel = nameSpace + ":version";
    public static final String commandChannel = nameSpace + ":commands";
}
