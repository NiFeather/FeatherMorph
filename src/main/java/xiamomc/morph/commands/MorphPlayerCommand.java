package xiamomc.morph.commands;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.messages.HelpStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.misc.DisguiseInfo;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.messages.FormattableMessage;

import java.util.ArrayList;
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

            sourcePlayer.performCommand("morph player:" + targetName);

            var config = morphManager.getPlayerConfiguration(sourcePlayer);

            if (!config.shownMorphPlayerMessageOnce)
            {
                sender.sendMessage(MessageUtils.prefixes(sender,
                        Component.translatable("PS: morphplayer已经合并进了morph指令")));

                sender.sendMessage(MessageUtils.prefixes(sender,
                        Component.translatable("PS: 现在执行此指令将自动转换为 /morph player:<玩家名>")));

                config.shownMorphPlayerMessageOnce = true;
            }
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
                    .stream().filter(DisguiseInfo::isPlayerDisguise).toList();

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
