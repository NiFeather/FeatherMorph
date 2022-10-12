package xiamomc.morph.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.messages.HelpStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.messages.FormattableMessage;

import java.util.ArrayList;
import java.util.List;

public class MorphCommand extends MorphPluginObject implements IPluginCommand
{
    @Resolved
    private MorphManager morphManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (sender instanceof Player player)
        {
            //伪装冷却
            if (!morphManager.canMorph(player))
            {
                sender.sendMessage(MessageUtils.prefixes(player, MorphStrings.disguiseCoolingDownString()));

                return true;
            }

            if (args.length >= 1)
            {
                morphManager.morphEntityTypeAuto(player, args[0], player.getTargetEntity(5));
            }
            else
                sender.sendMessage(MessageUtils.prefixes(sender, MorphStrings.disguiseNotDefinedString()));
        }

        return true;
    }

    @Override
    public String getCommandName()
    {
        return "morph";
    }

    @Resolved
    private MorphManager morphs;

    @Override
    public List<String> onTabComplete(List<String> args, CommandSender source)
    {
        var list = new ArrayList<String>();

        if (args.size() > 1) return list;

        if (source instanceof Player player)
        {
            //Logger.warn("BUFFERS: " + Arrays.toString(buffers));

            var arg = args.get(0);

            var infos = morphs.getAvaliableDisguisesFor(player);

            for (var di : infos)
            {
                var name = di.getKey();
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
        return HelpStrings.morphDescription();
    }
}
