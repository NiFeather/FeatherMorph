package xyz.nifeather.morph.commands.subcommands.plugin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.messages.CommandStrings;
import xyz.nifeather.morph.messages.HelpStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

import java.util.List;

public class QuerySubCommand extends MorphPluginObject implements ISubCommand
{
    @Override
    public String getCommandName()
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
    public List<String> onTabComplete(List<String> args, CommandSender sender)
    {
        var list = new ObjectArrayList<String>();
        if (args.size() > 1) return list;

        var name = args.size() == 1 ? args.get(0) : "";

        for (Player onlinePlayer : Bukkit.getOnlinePlayers())
        {
            var playerName = onlinePlayer.getName();
            if (playerName.toLowerCase().startsWith(name.toLowerCase())) list.add(playerName);
        }

        return list;
    }

    @Resolved
    private MorphManager manager;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull String[] args)
    {
        if (args.length >= 1)
        {
            var targetPlayer = Bukkit.getPlayerExact(args[0]);
            String locale = null;

            if (commandSender instanceof Player player)
                locale = MessageUtils.getLocale(player);

            if (targetPlayer != null)
            {
                var state = manager.getDisguiseStateFor(targetPlayer);

                if (state != null)
                {
                    commandSender.sendMessage(MessageUtils.prefixes(commandSender,
                            CommandStrings.qDisguisedString()
                                    .withLocale(locale)
                                    .resolve("who", targetPlayer.getName())
                                    .resolve("what", state.getDisguiseIdentifier())
                                    .resolve("storage_status",
                                            state.showingDisguisedItems()
                                                    ? CommandStrings.qaShowingDisguisedItemsString()
                                                    : CommandStrings.qaNotShowingDisguisedItemsString(),
                                            null)
                    ));
                }
                else
                {
                    commandSender.sendMessage(MessageUtils.prefixes(commandSender,
                            CommandStrings.qNotDisguisedString().resolve("who", targetPlayer.getName())));
                }
            }
        }
        return true;
    }
}
