package xyz.nifeather.morph.commands;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.messages.HelpStrings;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.DisguiseTypes;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.List;

public class MorphPlayerCommand extends MorphPluginObject implements IPluginCommand
{
    @Resolved
    private MorphManager morphManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (sender instanceof Player sourcePlayer)
        {
            var targetName = args.length >= 1 ? args[0] : "";

            sourcePlayer.performCommand("morph" + " " + DisguiseTypes.PLAYER.toId(targetName));
        }

        return true;
    }

    @Override
    public String getCommandName()
    {
        return "morphplayer";
    }

    @Override
    public List<String> onTabComplete(List<String> args, CommandSender source)
    {
        var list = new ObjectArrayList<String>();

        if (args.size() > 1) return list;

        if (source instanceof Player player)
        {
            var arg = args.get(0).toLowerCase();

            var infos = morphManager.getAvaliableDisguisesFor(player)
                    .stream().filter(DisguiseMeta::isPlayerDisguise).toList();

            for (var di : infos)
            {
                var name = di.playerDisguiseTargetName;
                if (!name.toLowerCase().contains(arg.toLowerCase())) continue;

                list.add(name);
            }
        }

        return list;
    }

    @Override
    public String getPermissionRequirement()
    {
        return null;
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.morphPlayerDescription();
    }
}
