package xyz.nifeather.morph.commands.subcommands.plugin;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.commands.brigadier.BrigadierCommand;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.messages.strings.HelpStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.TypesString;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

public class QueryAllSubCommand extends BrigadierCommand
{
    @Override
    public String name()
    {
        return "queryall";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.queryAllDescription();
    }

    @Override
    public String getPermissionRequirement()
    {
        return CommonPermissions.QUERY_STATES;
    }

    @Resolved
    private MorphManager manager;

    @Override
    public void registerAsChild(ArgumentBuilder<CommandSourceStack, ?> parentBuilder)
    {
        parentBuilder.then(
                Commands.literal(name())
                        .requires(this::checkPermission)
                        .executes(this::executes)
        );

        super.registerAsChild(parentBuilder);
    }

    public int executes(CommandContext<CommandSourceStack> context)
    {
        var list = manager.getActiveDisguises();
        var offlineStates = manager.availableOfflineDisguises();

        var commandSender = context.getSource().getSender();

        if (list.isEmpty() && offlineStates.isEmpty())
        {
            MessageUtils.send(commandSender, CommandStrings.qaNoBodyDisguisingString());
            return 1;
        }

        var msg = CommandStrings.qaDisguisedString();

        for (var i : list)
        {
            var player = i.getPlayer();
            msg.resolve("who", player.getName())
                    .resolve("status", player.isOnline()
                            ? CommandStrings.qaOnlineString()
                            : CommandStrings.qaOfflineString())
                    .resolve("what", i.getDisguiseIdentifier())
                    .resolve("storage_status", i.showingDisguisedItems()
                            ? CommandStrings.qaShowingDisguisedItemsString()
                            : CommandStrings.qaNotShowingDisguisedItemsString());

            MessageUtils.send(commandSender, msg);
        }

        msg = CommandStrings.listCount()
                .resolve("what", TypesString.savedDisguises())
                .resolve("amount", offlineStates.size());

        MessageUtils.send(commandSender, msg);

        return 1;
    }
}
