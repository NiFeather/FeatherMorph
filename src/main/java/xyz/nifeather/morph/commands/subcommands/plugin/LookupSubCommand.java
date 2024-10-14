package xyz.nifeather.morph.commands.subcommands.plugin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.messages.CommandStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

import java.util.List;

public class LookupSubCommand extends MorphPluginObject implements ISubCommand
{
    @Override
    public @NotNull String getCommandName()
    {
        return "lookup";
    }

    @Override
    public @Nullable String getPermissionRequirement()
    {
        return CommonPermissions.LOOKUP;
    }

    /**
     * 获取此指令的帮助信息
     *
     * @return 帮助信息
     */
    @Override
    public FormattableMessage getHelpMessage()
    {
        return new FormattableMessage(plugin, "lookup");
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphManager manager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (args.length == 0) return false;

        var targetLookupName = args[0];
        var offlinePlayer = Bukkit.getOfflinePlayer(targetLookupName);
        var targetLookupKey = args.length >= 2 ? args[1] : "any";

        var configuration = manager.getPlayerMeta(offlinePlayer);

        List<String> matches;

        //filter keys
        if (!targetLookupKey.equals("any"))
        {
            matches = configuration.getUnlockedDisguiseIdentifiers()
                    .stream().filter(k -> k.toUpperCase().contains(targetLookupKey.toUpperCase()))
                    .toList();
        }
        else
        {
            matches = configuration.getUnlockedDisguiseIdentifiers().clone();
        }

        sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.lookupFilterCommand()));
        matches.forEach(m ->
        {
            sender.sendMessage(MessageUtils.prefixes(sender, m));
        });

        return true;
    }
}
