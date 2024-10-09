package xyz.nifeather.morph.commands.subcommands.request;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.interfaces.IManageRequests;
import xyz.nifeather.morph.messages.CommonStrings;
import xyz.nifeather.morph.messages.HelpStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.RequestStrings;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.List;

public class SendSubCommand extends MorphPluginObject implements ISubCommand
{
    @Resolved
    private IManageRequests requests;

    @Resolved
    private MorphManager morphs;

    @Override
    public List<String> onTabComplete(List<String> args, CommandSender source)
    {
        var list = new ObjectArrayList<String>();

        if (source instanceof Player player)
        {
            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
                    .forEach(p -> list.add(p.getName()));
        }

        return list;
    }

    @Override
    public String getCommandName()
    {
        return "send";
    }

    @Override
    public String getPermissionRequirement()
    {
        return null;
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.requestSendDescription();
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

        if (targetPlayer.getUniqueId().equals(sourcePlayer.getUniqueId()))
        {
            sourcePlayer.sendMessage(MessageUtils.prefixes(sender, RequestStrings.cantSendToSelfString()));
            return true;
        }

        if (morphs.getAvaliableDisguisesFor(sourcePlayer).stream()
                .anyMatch(c -> c.isPlayerDisguise() && c.playerDisguiseTargetName.equals(args[0])))
        {
            sourcePlayer.sendMessage(MessageUtils.prefixes(sender, RequestStrings.alreadyHaveDisguiseString()));
            return true;
        }

        requests.createRequest(sourcePlayer, targetPlayer);

        return true;
    }
}
