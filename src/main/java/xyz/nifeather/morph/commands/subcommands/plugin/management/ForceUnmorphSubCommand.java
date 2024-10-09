package xyz.nifeather.morph.commands.subcommands.plugin.management;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.messages.CommandStrings;
import xyz.nifeather.morph.messages.CommonStrings;
import xyz.nifeather.morph.messages.HelpStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.List;
import java.util.Objects;

public class ForceUnmorphSubCommand extends MorphPluginObject implements ISubCommand
{
    @Override
    public @NotNull String getCommandName()
    {
        return "unmorph";
    }

    @Override
    public @Nullable String getPermissionRequirement()
    {
        return "xiamomc.morph.manage.unmorph";
    }

    @Resolved
    private MorphManager manager;

    @Override
    public @Nullable List<String> onTabComplete(List<String> args, CommandSender source)
    {
        var list = new ObjectArrayList<String>();
        if (args.size() != 1) return list;

        var name = args.get(0);

        var onlinePlayers = Bukkit.getOnlinePlayers();

        if (name.isBlank())
            list.add("*");

        for (var p : onlinePlayers)
            if (p.getName().toLowerCase().contains(name.toLowerCase())) list.add(p.getName());

        return list;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, String[] args)
    {
        if (args.length < 1)
        {
            sender.sendMessage(MessageUtils.prefixes(sender, CommonStrings.playerNotDefinedString()));

            return true;
        }

        if (Objects.equals(args[0], "*"))
        {
            manager.unMorphAll(true);
            sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.unMorphedAllString()));

            return true;
        }

        var player = Bukkit.getPlayerExact(args[0]);
        if (player == null)
        {
            sender.sendMessage(MessageUtils.prefixes(sender, CommonStrings.playerNotFoundString()));

            return true;
        }

        manager.unMorph(sender, player, true, true);

        sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.unMorphedSomeoneString()
                .resolve("who", player.getName())));

        return true;
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.manageUnmorphDescription();
    }
}
