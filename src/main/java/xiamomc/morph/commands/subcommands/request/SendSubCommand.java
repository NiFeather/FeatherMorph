package xiamomc.morph.commands.subcommands.request;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.interfaces.IManageRequests;
import xiamomc.morph.messages.CommonStrings;
import xiamomc.morph.messages.HelpStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.RequestStrings;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.messages.FormattableMessage;

import java.util.ArrayList;
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
        var list = new ArrayList<String>();

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
        if (sender instanceof Player sourcePlayer)
        {
            if (args.length >= 1)
            {
                var targetPlayer = Bukkit.getPlayer(args[0]);

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
            }
            else
            {
                sender.sendMessage(MessageUtils.prefixes(sender, CommonStrings.playerNotDefinedString()));
            }
        }

        return true;
    }
}
