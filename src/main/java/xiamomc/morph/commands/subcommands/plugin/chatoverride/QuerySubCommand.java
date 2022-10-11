package xiamomc.morph.commands.subcommands.plugin.chatoverride;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.messages.CommandStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;

public class QuerySubCommand extends MorphPluginObject implements ISubCommand
{
    @Override
    public @NotNull String getCommandName()
    {
        return "query";
    }

    @Resolved
    private MorphManager manager;

    @Override
    public String getPermissionRequirement()
    {
        return "xiamomc.morph.chatoverride.query";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        sender.sendMessage(MessageUtils.prefixes(sender, manager.allowChatOverride()
                ? CommandStrings.chatOverrideEnabledString()
                : CommandStrings.chatOverrideDisabledString()));

        return true;
    }

    @Override
    public @Nullable String getHelpMessage()
    {
        return "获取状态";
    }
}
