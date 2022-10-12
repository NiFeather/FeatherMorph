package xiamomc.morph.commands.subcommands.plugin;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.messages.CommandStrings;
import xiamomc.morph.messages.HelpStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.messages.FormattableMessage;

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
        var list = manager.getDisguisedPlayers();
        var offlineStates = manager.getAvaliableOfflineStates();

        if (list.size() == 0 && offlineStates.size() == 0)
        {
            commandSender.sendMessage(MessageUtils.prefixes(commandSender, CommandStrings.qaNoBodyDisguisingString()));
            return true;
        }

        var msg = CommandStrings.qaDisguisedString();

        for (var i : list)
        {
            var player = i.getPlayer();
            msg.resolve("who", player.getName())
                    .resolve("status", player.isOnline()
                            ? CommandStrings.qaOnlineString()
                            : CommandStrings.qaOfflineString())
                    .resolve("what", i.getDisplayName())
                    .resolve("storage_status", i.showingDefaultItems()
                            ? CommandStrings.qaShowingDisguisedItemsString()
                            : CommandStrings.qaNotShowingDisguisedItemsString());

            commandSender.sendMessage(MessageUtils.prefixes(commandSender, msg));
        }

        for (var s : offlineStates)
        {
            commandSender.sendMessage(MessageUtils.prefixes(commandSender,
                    msg.resolve("who", s.playerName)
                            .resolve("status", CommandStrings.qaIsOfflineStoreString())
                            .resolve("storage_status", "")
                            .resolve("what", s.disguiseID)));
        }

        return true;
    }
}
