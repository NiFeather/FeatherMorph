package xiamomc.morph.commands.subcommands.plugin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.libraryaddict.disguise.DisguiseAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.messages.CommandStrings;
import xiamomc.morph.messages.HelpStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;

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
        return "xiamomc.morph.query";
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

            if (targetPlayer != null)
            {
                var info = manager.getDisguiseStateFor(targetPlayer);

                if (info != null)
                    commandSender.sendMessage(MessageUtils.prefixes(commandSender,
                            CommandStrings.qDisguisedString()
                                    .resolve("who", targetPlayer.getName())
                                    .resolve("what", info.getDisplayName())
                                    .resolve("storage_status", info.showingDisguisedItems()
                                            ? CommandStrings.qaShowingDisguisedItemsString()
                                            : CommandStrings.qaNotShowingDisguisedItemsString())
                    ));
                else if (DisguiseAPI.isDisguised(targetPlayer))
                {
                    commandSender.sendMessage(MessageUtils.prefixes(commandSender,
                            CommandStrings.qDisguisedUnManageableString()
                                    .resolve("who", targetPlayer.getName())
                                    .resolve("what", DisguiseAPI.getDisguise(targetPlayer).getDisguiseName())
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
