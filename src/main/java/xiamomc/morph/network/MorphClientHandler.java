package xiamomc.morph.network;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.sqlite.Collation;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.pluginbase.Annotations.Initializer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

public class MorphClientHandler extends MorphPluginObject
{
    @Initializer
    private void load(MorphPlugin plugin, MorphManager manager)
    {
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, initializeChannel, (cN, player, data) ->
        {
            player.sendPluginMessage(plugin, initializeChannel, "".getBytes());

            clientPlayers.add(player);
        });

        var apiVersionBytes = ByteBuffer.allocate(4).putInt(plugin.clientApiVersion).array();
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, versionChannel, (cN, player, data) ->
        {
            if (!clientPlayers.contains(player)) return;

            player.sendPluginMessage(plugin, versionChannel, apiVersionBytes);
        });

        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, commandChannel, (cN, player, data) ->
        {
            if (!clientPlayers.contains(player)) return;

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
                        player.sendPluginMessage(plugin, commandChannel, "deny morph".getBytes());
                }
                case "toggleself" -> manager.setSelfDisguiseVisible(player, !manager.getPlayerConfiguration(player).showDisguiseToSelf, true);
                case "morph" ->
                {
                    if (str.length == 2)
                    {
                        var subCommands = str[1];

                        if (manager.canMorph(player))
                            manager.morph(player, subCommands, player.getTargetEntity(5));
                        else
                            player.sendPluginMessage(plugin, commandChannel, "deny morph".getBytes());
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

                    var list = manager.getPlayerConfiguration(player).getUnlockedDisguiseIdentifiers();
                    this.refreshPlayerClientMorphs(list, player);

                    var state = manager.getDisguiseStateFor(player);

                    if (state != null)
                        updateCurrentIdentifier(player, state.getDisguiseIdentifier());

                    initialzedPlayers.add(player);
                }
            }
        });

        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, initializeChannel);
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, versionChannel);
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, commandChannel);
    }

    private final List<Player> initialzedPlayers = new ObjectArrayList<>();

    private final List<Player> clientPlayers = new ObjectArrayList<>();

    public void refreshPlayerClientMorphs(List<String> identifiers, Player player)
    {
        var additBuilder = new StringBuilder();

        additBuilder.append("query").append(" ").append("set").append(" ");

        for (var s : identifiers)
            additBuilder.append(s).append(" ");

        player.sendPluginMessage(plugin, commandChannel, additBuilder.toString().getBytes());
    }

    public void sendDiff(@Nullable List<String> addits, @Nullable List<String> removal, Player player)
    {
        if (addits != null)
        {
            var additBuilder = new StringBuilder();

            additBuilder.append("query").append(" ").append("add").append(" ");

            for (var s : addits)
                additBuilder.append(s).append(" ");

            player.sendPluginMessage(plugin, commandChannel, additBuilder.toString().getBytes());
        }

        if (removal != null)
        {
            var removalBuilder = new StringBuilder();

            removalBuilder.append("query").append(" ").append("remove").append(" ");

            for (var rs : removal)
                removalBuilder.append(rs).append(" ");

            player.sendPluginMessage(plugin, commandChannel, removalBuilder.toString().getBytes());
        }
    }

    public void updateCurrentIdentifier(Player player, String str)
    {
        var builder = new StringBuilder();

        builder.append("current");

        if (str != null)
            builder.append(" ").append(str);

        player.sendPluginMessage(plugin, commandChannel, builder.toString().getBytes());
    }

    public void unInitializePlayer(Player player)
    {
        this.clientPlayers.remove(player);
    }

    public void sendReAuth(Collection<? extends Player> players)
    {
        players.forEach(p -> p.sendPluginMessage(plugin, commandChannel, "reauth".getBytes()));
    }

    public void sendUnAuth(Collection<? extends Player> players)
    {
        players.forEach(p -> p.sendPluginMessage(plugin, commandChannel, "unauth".getBytes()));
    }

    private static final String nameSpace = MorphPlugin.getMorphNameSpace();

    public static final String initializeChannel = nameSpace + ":init";
    public static final String versionChannel = nameSpace + ":version";
    public static final String commandChannel = nameSpace + ":commands";
}
