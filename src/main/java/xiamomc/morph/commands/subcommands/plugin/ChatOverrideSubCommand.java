package xiamomc.morph.commands.subcommands.plugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.commands.subcommands.plugin.chatoverride.QuerySubCommand;
import xiamomc.morph.commands.subcommands.plugin.chatoverride.ToggleSubCommand;
import xiamomc.pluginbase.Command.ISubCommand;

import java.util.List;

public class ChatOverrideSubCommand extends MorphPluginObject implements ISubCommand
{
    @Override
    public @NotNull String getCommandName()
    {
        return "chatoverride";
    }

    private final List<ISubCommand> subCommands = List.of(
            new QuerySubCommand(),
            new ToggleSubCommand()
    );

    @Override
    public String getPermissionRequirement()
    {
        return "xiamomc.morph.chatoverride";
    }

    @Override
    public List<ISubCommand> getSubCommands()
    {
        return subCommands;
    }

    @Override
    public @Nullable String getHelpMessage()
    {
        return "查看服务器的聊天覆盖状态";
    }
}
