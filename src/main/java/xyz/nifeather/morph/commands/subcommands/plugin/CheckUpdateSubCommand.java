package xyz.nifeather.morph.commands.subcommands.plugin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.UpdateStrings;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.updates.UpdateHandler;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;

public class CheckUpdateSubCommand extends MorphPluginObject implements ISubCommand
{
    @Override
    public @NotNull String getCommandName()
    {
        return "check_update";
    }

    @Override
    public @Nullable String getPermissionRequirement()
    {
        return CommonPermissions.CHECK_UPDATE;
    }

    /**
     * 获取此指令的帮助信息
     *
     * @return 帮助信息
     */
    @Override
    public FormattableMessage getHelpMessage()
    {
        return new FormattableMessage(plugin, "update");
    }

    @Resolved
    private UpdateHandler handler;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        sender.sendMessage(MessageUtils.prefixes(sender, UpdateStrings.checkingUpdate()));
        handler.checkUpdate(true, result ->
                this.onRequestFinish(result, sender), sender);

        return true;
    }

    private void onRequestFinish(UpdateHandler.CheckResult result, CommandSender sender)
    {
        if (result == UpdateHandler.CheckResult.ALREADY_LATEST)
        {
            var msg = UpdateStrings.noNewVersionAvailable().resolve("mc_version", Bukkit.getMinecraftVersion());
            sender.sendMessage(MessageUtils.prefixes(sender, msg));
        }
    }
}
