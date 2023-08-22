package xiamomc.morph.commands.subcommands.plugin;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.commands.MorphPluginCommand;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.UpdateStrings;
import xiamomc.morph.misc.UpdateHandler;
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
                this.onRequestFinish(result, sender));

        return true;
    }

    private void onRequestFinish(UpdateHandler.CheckResult result, CommandSender sender)
    {
        if (result == UpdateHandler.CheckResult.ALREADY_LATEST)
            sender.sendMessage(MessageUtils.prefixes(sender, UpdateStrings.noNewVersionAvailable()));
    }
}