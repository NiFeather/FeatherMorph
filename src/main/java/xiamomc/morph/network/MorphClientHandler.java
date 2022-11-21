package xiamomc.morph.network;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.pluginbase.Annotations.Initializer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MorphClientHandler extends MorphPluginObject
{
    @Initializer
    private void load(MorphPlugin plugin, MorphManager manager)
    {
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, initializeChannel, (cN, player, data) ->
        {
            if (initializedPlayers.contains(player))
            {
                player.sendPluginMessage(plugin, initializeChannel, "no".getBytes());
            }
            else
            {
                var list = manager.getPlayerConfiguration(player).getUnlockedDisguiseIdentifiers();
                this.refreshPlayerClientMorphs(list, player);
            }
        });

        var apiVersionBytes = ByteBuffer.allocate(4).putInt(plugin.clientApiVersion).array();
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, versionChannel, (cN, player, data) ->
        {
            player.sendPluginMessage(plugin, versionChannel, apiVersionBytes);
        });

        Bukkit.getMessenger().registerIncomingPluginChannel(plugin, abilityChannel, (cN, player, data) ->
        {
            if (noOpPlayers.contains(player)) return;

            var str = new String(data, StandardCharsets.UTF_8).split(" ", 2);

            if (str.length < 1) return;

            var baseCommand = str[0];

            switch (baseCommand)
            {
                case "skill" -> manager.executeDisguiseSkill(player);
                case "unmorph" -> manager.unMorph(player);
                case "toggleself" -> manager.setSelfDisguiseVisible(player, !manager.getPlayerConfiguration(player).showDisguiseToSelf, true);
                case "morph" ->
                {
                    if (str.length == 2)
                    {
                        var subCommands = str[1];

                        if (manager.canMorph(player))
                            manager.morph(player, subCommands, player.getTargetEntity(5));
                    }
                    else
                    {
                        manager.doQuickDisguise(player);
                    }
                }
            }

            noOpPlayers.add(player);
        });

        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, initializeChannel);
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, versionChannel);
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin, abilityChannel);

        this.addSchedule(c -> update());
    }

    public void refreshPlayerClientMorphs(List<String> identifiers, Player player)
    {
        var additBuilder = new StringBuilder();

        additBuilder.append("query").append(" ").append("set").append(" ");

        for (var s : identifiers)
            additBuilder.append(s).append(" ");

        player.sendPluginMessage(plugin, abilityChannel, additBuilder.toString().getBytes());
    }

    public void sendDiff(@Nullable List<String> addits, @Nullable List<String> removal, Player player)
    {
        if (addits != null)
        {
            var additBuilder = new StringBuilder();

            additBuilder.append("query").append(" ").append("add").append(" ");

            for (var s : addits)
                additBuilder.append(s).append(" ");

            player.sendPluginMessage(plugin, abilityChannel, additBuilder.toString().getBytes());
        }

        if (removal != null)
        {
            var removalBuilder = new StringBuilder();

            removalBuilder.append("query").append(" ").append("remove").append(" ");

            for (var rs : removal)
                removalBuilder.append(rs).append(" ");

            player.sendPluginMessage(plugin, abilityChannel, removalBuilder.toString().getBytes());
        }
    }

    private final List<Player> noOpPlayers = new ObjectArrayList<>();

    private final List<Player> initializedPlayers = new ObjectArrayList<>();

    public void unInitializePlayer(Player player)
    {
        this.initializedPlayers.remove(player);
    }

    private void update()
    {
        noOpPlayers.clear();
        this.addSchedule(c -> update());
    }

    private static final String nameSpace = MorphPlugin.getMorphNameSpace();

    public static final String initializeChannel = nameSpace + ":init";
    public static final String versionChannel = nameSpace + ":version";
    public static final String abilityChannel = nameSpace + ":commands";
}
