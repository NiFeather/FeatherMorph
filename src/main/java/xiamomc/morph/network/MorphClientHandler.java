package xiamomc.morph.network;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.game.PacketPlayOutCustomPayload;
import net.minecraft.resources.MinecraftKey;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class MorphClientHandler extends MorphPluginObject
{
    private final Bindable<Boolean> allowClient = new Bindable<>(false);
    private final Bindable<Boolean> logInComingPackets = new Bindable<>(false);
    private final Bindable<Boolean> logOutGoingPackets = new Bindable<>(false);

    //部分来自 CraftPlayer#sendPluginMessage(), 在我们搞清楚到底为什么服务端会吞包之前先这样
    private void sendPacket(String channel, Player player, byte[] message)
    {
        if (logOutGoingPackets.get())
            logger.info(channel + " :: " + player.getName() + " -> " + new String(message, StandardCharsets.UTF_8));

        StandardMessenger.validatePluginMessage(Bukkit.getServer().getMessenger(), plugin, channel, message);

        var nmsPlayer = ((CraftPlayer) player).getHandle();

        var packet = new PacketPlayOutCustomPayload(new MinecraftKey(channel), new PacketDataSerializer(Unpooled.wrappedBuffer(message)));
        nmsPlayer.b.a(packet);
    }

    @Resolved
    private MorphManager manager;

    @Initializer
    private void load(MorphPlugin plugin, MorphConfigManager configManager)
    {
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, initializeChannel, (cN, player, data) ->
        {
            if (!allowClient.get()) return;

            if (logInComingPackets.get())
                logger.info("收到了来自" + player.getName() + "的初始化消息：" + new String(data, StandardCharsets.UTF_8));

            this.sendPacket(initializeChannel, player, "".getBytes());

            clientPlayers.add(player);
        });

        var apiVersionBytes = ByteBuffer.allocate(4).putInt(plugin.clientApiVersion).array();
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, versionChannel, (cN, player, data) ->
        {
            if (!allowClient.get()) return;

            if (!clientPlayers.contains(player)) return;

            if (logInComingPackets.get())
                logger.info("收到了来自" + player.getName() + "的API请求：" + new String(data, StandardCharsets.UTF_8));

            this.sendPacket(versionChannel, player, apiVersionBytes);
        });

        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, commandChannel, (cN, player, data) ->
        {
            if (!allowClient.get()) return;

            if (!clientPlayers.contains(player)) return;

            if (logInComingPackets.get())
                logger.info("在" + cN + "收到了来自" + player.getName() + "的服务端指令：" + new String(data, StandardCharsets.UTF_8));

            var str = new String(data, StandardCharsets.UTF_8).split(" ", 2);

            if (str.length < 1) return;

            var baseCommand = str[0];

            switch (baseCommand)
            {
                case "skill" ->
                {
                    var state = manager.getDisguiseStateFor(player);

                    if (state != null && state.getSkillCooldown() <= 0)
                        manager.executeDisguiseSkill(player);
                }
                case "unmorph" ->
                {
                    if (manager.getDisguiseStateFor(player) != null)
                        manager.unMorph(player);
                    else
                        sendClientCommand(player, ClientCommands.denyOperationCommand("morph"));
                }
                case "toggleself" ->
                {
                    if (str.length != 2) return;

                    var subData = str[1].split(" ");

                    if (subData.length < 1) return;

                    //获取客户端选项
                    var playerOption = getPlayerOption(player);
                    var playerConfig = manager.getPlayerConfiguration(player);

                    if (subData[0].equals("client"))
                    {
                        if (subData.length < 2) return;

                        var isClient = Boolean.parseBoolean(subData[1]);

                        playerOption.setClientSideSelfView(isClient);

                        //如果客户端打开了本地预览，则隐藏伪装，否则显示伪装
                        var state = manager.getDisguiseStateFor(player);
                        if (state != null) state.setSelfVisible(!isClient && playerConfig.showDisguiseToSelf);
                    }
                    else
                    {
                        var val = Boolean.parseBoolean(subData[0]);

                        if (val == playerConfig.showDisguiseToSelf) return;
                        manager.setSelfDisguiseVisible(player, val, true, playerOption.isClientSideSelfView(), false);
                    }
                }
                case "morph" ->
                {
                    if (str.length == 2)
                    {
                        var subCommands = str[1];

                        if (manager.canMorph(player))
                            manager.morph(player, subCommands, player.getTargetEntity(5));
                        else
                            sendClientCommand(player, ClientCommands.denyOperationCommand("morph"));
                    }
                    else
                    {
                        manager.doQuickDisguise(player, null);
                    }
                }
                case "initial" ->
                {
                    if (playerStateMap.getOrDefault(player, null) != ConnectionState.JOINED)
                        playerStateMap.put(player, ConnectionState.CONNECTING);

                    //等待玩家加入再发包
                    this.waitUntilReady(player, () ->
                    {
                        if (initializedPlayers.contains(player))
                            return;

                        var config = manager.getPlayerConfiguration(player);
                        var list = config.getUnlockedDisguiseIdentifiers();
                        this.refreshPlayerClientMorphs(list, player);

                        var state = manager.getDisguiseStateFor(player);

                        if (state != null)
                            manager.refreshClientState(state);

                        sendClientCommand(player, ClientCommands.setToggleSelfCommand(config.showDisguiseToSelf));
                        initializedPlayers.add(player);
                    });
                }
                case "option" ->
                {
                    if (str.length < 2) return;

                    var node = str[1].split(" ", 2);

                    if (node.length < 2) return;

                    var baseName = node[0];
                    var value = node[1];

                    switch (baseName)
                    {
                        case "clientview" ->
                        {
                            var val = Boolean.parseBoolean(value);

                            this.getPlayerOption(player).setClientSideSelfView(val);

                            var state = manager.getDisguiseStateFor(player);
                            if (state != null) state.setSelfVisible(!val);
                        }
                    }
                }
            }
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

            initializedPlayers.removeAll(players);
            clientPlayers.removeAll(players);

            if (n)
                this.sendReAuth(players);
            else
                this.sendUnAuth(players);
        });

        Bukkit.getOnlinePlayers().forEach(p -> playerStateMap.put(p, ConnectionState.JOINED));
    }

    //region wait until ready
    private void waitUntilReady(Player player, Runnable r)
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

    private final List<Player> initializedPlayers = new ObjectArrayList<>();

    private final List<Player> clientPlayers = new ObjectArrayList<>();

    /**
     * 检查某个玩家是否使用客户端加入
     *
     * @param player 目标玩家
     * @return 玩家是否使用客户端加入
     * @apiNote 此API只能检查客户端是否已连接，检查初始化状态请使用 {@link MorphClientHandler#clientInitialized(Player)}
     */
    public boolean clientConnected(Player player)
    {
        return clientPlayers.contains(player);
    }

    /**
     * 检查某个玩家的客户端是否已初始化
     *
     * @param player 目标玩家
     * @return 此玩家的客户端是否已初始化
     */
    public boolean clientInitialized(Player player)
    {
        return initializedPlayers.contains(player);
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

        var additBuilder = new StringBuilder();

        additBuilder.append("query").append(" ").append("set").append(" ");

        for (var s : identifiers)
            additBuilder.append(s).append(" ");

        sendClientCommand(player, additBuilder.toString());
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
        {
            var additBuilder = new StringBuilder();

            additBuilder.append("query").append(" ").append("add").append(" ");

            for (var s : addits)
                additBuilder.append(s).append(" ");

            sendClientCommand(player, additBuilder.toString());
        }

        if (removal != null)
        {
            var removalBuilder = new StringBuilder();

            removalBuilder.append("query").append(" ").append("remove").append(" ");

            for (var rs : removal)
                removalBuilder.append(rs).append(" ");

            sendClientCommand(player, removalBuilder.toString());
        }
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

        var builder = new StringBuilder();

        builder.append("current");

        if (str != null)
            builder.append(" ").append(str);

        sendClientCommand(player, builder.toString());
    }

    /**
     * 反初始化玩家
     *
     * @param player 目标玩家
     */
    public void unInitializePlayer(Player player)
    {
        this.clientPlayers.remove(player);
        this.playerOptionMap.clear();
        playerStateMap.remove(player);
        initializedPlayers.remove(player);

        var playerConfig = manager.getPlayerConfiguration(player);
        var state = manager.getDisguiseStateFor(player);
        if (state != null) state.setSelfVisible(playerConfig.showDisguiseToSelf);
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
            sendClientCommand(p, "reauth");
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
            sendClientCommand(p, "unauth", true);
            unInitializePlayer(p);
        });
    }

    private void sendClientCommand(Player player, String cmd, boolean overrideClientSetting)
    {
        if (cmd == null || cmd.isEmpty() || cmd.isBlank()) return;

        if (!allowClient.get() && !overrideClientSetting) return;

        this.sendPacket(commandChannel, player, cmd.getBytes());
    }

    /**
     * 向某一玩家的客户端发送指令
     *
     * @param player 目标玩家
     * @param cmd 指令内容
     */
    public void sendClientCommand(Player player, String cmd)
    {
        this.sendClientCommand(player, cmd, false);
    }

    private static final String nameSpace = MorphPlugin.getMorphNameSpace();

    public static final String initializeChannel = nameSpace + ":init";
    public static final String versionChannel = nameSpace + ":version";
    public static final String commandChannel = nameSpace + ":commands";
}
