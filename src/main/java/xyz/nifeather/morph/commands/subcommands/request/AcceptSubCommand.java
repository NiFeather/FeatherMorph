package xyz.nifeather.morph.commands.subcommands.request;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.interfaces.IManageRequests;
import xyz.nifeather.morph.messages.CommonStrings;
import xyz.nifeather.morph.messages.HelpStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.List;

public class AcceptSubCommand extends MorphPluginObject implements ISubCommand
{
    @Resolved
    private IManageRequests requests;

    @Override
    public List<String> onTabComplete(List<String> args, CommandSender source)
    {
        var list = new ObjectArrayList<String>();

        if (source instanceof Player player)
        {
            var reqs = requests.getAvailableRequestsFor(player);

            reqs.forEach(r -> list.add(r.sourcePlayer.getName()));
        }

        return list;
    }

    @Override
    public String getCommandName()
    {
        return "accept";
    }

    @Override
    public String getPermissionRequirement()
    {
        return null;
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.requestAcceptDescription();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (!(sender instanceof Player sourcePlayer))
            return true;

        if (args.length < 1)
        {
            sender.sendMessage(MessageUtils.prefixes(sender, CommonStrings.playerNotDefinedString()));
            return true;
        }

        var targetPlayer = Bukkit.getPlayerExact(args[0]);

        if (targetPlayer == null)
        {
            sender.sendMessage(MessageUtils.prefixes(sender, CommonStrings.playerNotFoundString()));
            return true;
        }

        requests.acceptRequest(sourcePlayer, targetPlayer);
        return true;
    }
}
