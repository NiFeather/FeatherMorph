package xiamomc.morph.commands.subcommands.plugin.chatoverride;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.MessageUtils;
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
        sender.sendMessage(MessageUtils.prefixes(sender, Component.text("聊天覆盖当前已")
                .append(Component.text(manager.allowChatOverride() ? "启用" : "禁用").decorate(TextDecoration.BOLD))));

        return true;
    }

    @Override
    public @Nullable String getHelpMessage()
    {
        return "获取状态";
    }
}
