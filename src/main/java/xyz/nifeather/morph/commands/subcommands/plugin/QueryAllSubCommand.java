package xyz.nifeather.morph.commands.subcommands.plugin;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.messages.CommandStrings;
import xyz.nifeather.morph.messages.HelpStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;

public class QueryAllSubCommand extends MorphPluginObject implements ISubCommand
{
    @Override
    public String getCommandName()
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
        return "xiamomc.morph.query";
    }

    @Resolved
    private MorphManager manager;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull String[] strings)
    {
        var list = manager.getActiveDisguises();
        var offlineStates = manager.getAvaliableOfflineStates();

        if (list.size() == 0 && offlineStates.size() == 0)
        {
            commandSender.sendMessage(MessageUtils.prefixes(commandSender, CommandStrings.qaNoBodyDisguisingString()));
            return true;
        }

        var msg = CommandStrings.qaDisguisedString();
        var locale = MessageUtils.getLocale(commandSender);

        for (var i : list)
        {
            var player = i.getPlayer();
            msg.withLocale(locale)
                    .resolve("who", player.getName())
                    .resolve("status", player.isOnline()
                            ? CommandStrings.qaOnlineString()
                            : CommandStrings.qaOfflineString(), null)
                    .resolve("what", i.getDisguiseIdentifier())
                    .resolve("storage_status", i.showingDisguisedItems()
                            ? CommandStrings.qaShowingDisguisedItemsString()
                            : CommandStrings.qaNotShowingDisguisedItemsString(), null);

            commandSender.sendMessage(MessageUtils.prefixes(commandSender, msg));
        }

        for (var s : offlineStates)
        {
            commandSender.sendMessage(MessageUtils.prefixes(commandSender,
                    msg.withLocale(locale)
                            .resolve("who", s.playerName)
                            .resolve("status", CommandStrings.qaIsOfflineStoreString(), null)
                            .resolve("storage_status", "")
                            .resolve("what", s.disguiseID)));
        }

        return true;
    }
}
