package xyz.nifeather.morph.commands.subcommands.plugin;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Player;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.commands.brigadier.BrigadierCommand;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.messages.strings.HelpStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

public class QuerySubCommand extends BrigadierCommand
{
    @Override
    public String name()
    {
        return "query";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.queryDescription();
    }

    @Override
    public String getPermissionRequirement()
    {
        return CommonPermissions.QUERY_STATES;
    }

    @Override
    public void registerAsChild(ArgumentBuilder<CommandSourceStack, ?> parentBuilder)
    {
        parentBuilder.then(
                Commands.literal(name())
                        .requires(this::checkPermission)
                        .then(
                                Commands.argument("who", ArgumentTypes.players())
                                        .executes(this::executes)
                        )
        );

        super.registerAsChild(parentBuilder);
    }

    @Resolved
    private MorphManager manager;

    public int executes(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        var players = context.getArgument("who", PlayerSelectorArgumentResolver.class)
                .resolve(context.getSource());

        String locale = null;

        var commandSender = context.getSource().getSender();

        if (commandSender instanceof Player player)
            locale = MessageUtils.getLocale(player);

        if (players.isEmpty())
            return 0;

        for (Player targetPlayer : players)
        {
            var state = manager.getDisguiseStateFor(targetPlayer);

            if (state != null)
            {
                MessageUtils.send(commandSender,
                        CommandStrings.qDisguisedString()
                                .resolve("who", targetPlayer.getName())
                                .resolve("what", state.getDisguiseIdentifier())
                                .resolve("storage_status",
                                        state.showingDisguisedItems()
                                                ? CommandStrings.qaShowingDisguisedItemsString()
                                                : CommandStrings.qaNotShowingDisguisedItemsString()
                                )
                );
            }
            else
            {
                MessageUtils.send(commandSender,
                        CommandStrings.qNotDisguisedString().resolve("who", targetPlayer.getName()));
            }
        }

        return 1;
    }
}
