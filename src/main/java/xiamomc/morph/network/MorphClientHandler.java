package xiamomc.morph.network;

import com.destroystokyo.paper.ClientOption;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MorphClientHandler extends MorphPluginObject
{
    private final Bindable<Boolean> allowClient = new Bindable<>(false);
    private final Bindable<Boolean> logPackets = new Bindable<>(false);

    @Initializer
    private void load(MorphPlugin plugin, MorphManager manager, MorphConfigManager configManager)
    {
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, initializeChannel, (cN, player, data) ->
        {
            if (!allowClient.get()) return;

            if (logPackets.get())
                logger.info("收到了来自" + player.getName() + "的初始化消息：" + new String(data, StandardCharsets.UTF_8));

            player.sendPluginMessage(plugin, initializeChannel, "".getBytes());

            clientPlayers.add(player);
        });

        var apiVersionBytes = ByteBuffer.allocate(4).putInt(plugin.clientApiVersion).array();
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, versionChannel, (cN, player, data) ->
        {
            if (!allowClient.get()) return;

            if (!clientPlayers.contains(player)) return;

            if (logPackets.get())
                logger.info("收到了来自" + player.getName() + "的API请求：" + new String(data, StandardCharsets.UTF_8));

            player.sendPluginMessage(plugin, versionChannel, apiVersionBytes);
        });

        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, commandChannel, (cN, player, data) ->
        {
            if (!allowClient.get()) return;

            if (!clientPlayers.contains(player)) return;

            if (logPackets.get())
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
                        manager.doQuickDisguise(player);
                    }
                }
                case "initial" ->
                {
                    if (initialzedPlayers.contains(player))
                        return;

                    var config = manager.getPlayerConfiguration(player);
                    var list = config.getUnlockedDisguiseIdentifiers();
                    this.refreshPlayerClientMorphs(list, player);

                    var state = manager.getDisguiseStateFor(player);

                    if (state != null)
                        updateCurrentIdentifier(player, state.getDisguiseIdentifier());

                    sendClientCommand(player, ClientCommands.setToggleSelfCommand(config.showDisguiseToSelf));
                    initialzedPlayers.add(player);
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
                        }
                    }
                }
            }
        });

        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, initializeChannel);
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, versionChannel);
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, commandChannel);

        configManager.bind(allowClient, ConfigOption.ALLOW_CLIENT);
        configManager.bind(logPackets, ConfigOption.LOG_INCOMING_PACKETS);

        allowClient.onValueChanged((o, n) ->
        {
            var players = Bukkit.getOnlinePlayers();

            initialzedPlayers.removeAll(players);
            clientPlayers.removeAll(players);

            if (n)
                this.sendReAuth(players);
            else
                this.sendUnAuth(players);
        });
    }

    private final Map<UUID, MorphClientOptions> playerOptionMap = new Object2ObjectOpenHashMap<>();

    public MorphClientOptions getPlayerOption(Player player)
    {
        var uuid = player.getUniqueId();
        var option = playerOptionMap.getOrDefault(uuid, null);

        if (option != null) return option;

        option = new MorphClientOptions(player);
        playerOptionMap.put(uuid, option);

        return option;
    }

    private final List<Player> initialzedPlayers = new ObjectArrayList<>();

    private final List<Player> clientPlayers = new ObjectArrayList<>();

    public void refreshPlayerClientMorphs(List<String> identifiers, Player player)
    {
        if (!allowClient.get()) return;

        var additBuilder = new StringBuilder();

        additBuilder.append("query").append(" ").append("set").append(" ");

        for (var s : identifiers)
            additBuilder.append(s).append(" ");

        sendClientCommand(player, additBuilder.toString());
    }

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

    public void updateCurrentIdentifier(Player player, String str)
    {
        if (!allowClient.get()) return;

        var builder = new StringBuilder();

        builder.append("current");

        if (str != null)
            builder.append(" ").append(str);

        sendClientCommand(player, builder.toString());
    }

    public void unInitializePlayer(Player player)
    {
        this.clientPlayers.remove(player);
        this.playerOptionMap.clear();
    }

    public void sendReAuth(Collection<? extends Player> players)
    {
        if (!allowClient.get()) return;

        players.forEach(p -> sendClientCommand(p, "reauth"));
    }

    public void sendUnAuth(Collection<? extends Player> players)
    {
        players.forEach(p -> sendClientCommand(p, "unauth", true));
    }

    private void sendClientCommand(Player player, String cmd, boolean overrideClientSetting)
    {
        if (cmd == null || cmd.isEmpty() || cmd.isBlank()) return;

        if (!allowClient.get() && !overrideClientSetting) return;

        player.sendPluginMessage(plugin, commandChannel, cmd.getBytes());
    }

    public void sendClientCommand(Player player, String cmd)
    {
        this.sendClientCommand(player, cmd, false);
    }

    private static final String nameSpace = MorphPlugin.getMorphNameSpace();

    public static final String initializeChannel = nameSpace + ":init";
    public static final String versionChannel = nameSpace + ":version";
    public static final String commandChannel = nameSpace + ":commands";
}
