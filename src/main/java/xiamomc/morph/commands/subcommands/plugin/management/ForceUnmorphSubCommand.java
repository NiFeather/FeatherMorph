package xiamomc.morph.commands.subcommands.plugin.management;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.messages.CommandStrings;
import xiamomc.morph.messages.CommonStrings;
import xiamomc.morph.messages.HelpStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.messages.FormattableMessage;

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

        var player = Bukkit.getPlayer(args[0]);
        if (player == null)
        {
            sender.sendMessage(MessageUtils.prefixes(sender, CommonStrings.playerNotFoundString()));

            return true;
        }

        manager.unMorph(player);

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
