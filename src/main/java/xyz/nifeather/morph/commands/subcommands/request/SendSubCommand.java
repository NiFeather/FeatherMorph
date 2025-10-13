package xyz.nifeather.morph.commands.subcommands.request;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.commands.brigadier.IConvertibleBrigadier;
import xyz.nifeather.morph.interfaces.IManageRequests;
import xyz.nifeather.morph.messages.strings.HelpStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.RequestStrings;
import xyz.nifeather.morph.misc.DisguiseTypes;

public class SendSubCommand extends MorphPluginObject implements IConvertibleBrigadier
{
    @Resolved
    private IManageRequests requests;

    @Resolved
    private MorphManager morphs;

    public int executes(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        var sender = context.getSource().getSender();

        if (!(sender instanceof Player sourcePlayer))
            return Command.SINGLE_SUCCESS;

        var players = context.getArgument("who", PlayerSelectorArgumentResolver.class)
                .resolve(context.getSource());

        players.forEach(targetPlayer ->
        {
            if (targetPlayer.getUniqueId().equals(sourcePlayer.getUniqueId()))
            {
                MessageUtils.send(sender, RequestStrings.cantSendToSelfString());
                return;
            }

            var id = DisguiseTypes.PLAYER.toId(targetPlayer.getName());
            if (morphs.getAvaliableDisguisesFor(sourcePlayer).stream()
                    .anyMatch(c -> c.rawIdentifier.equals(id)))
            {
                MessageUtils.send(sender, RequestStrings.alreadyHaveDisguiseString());
                return;
            }

            requests.createRequest(sourcePlayer, targetPlayer);
        });

        return Command.SINGLE_SUCCESS;
    }

    @Override
    public String name()
    {
        return "send";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.requestSendDescription();
    }
}
