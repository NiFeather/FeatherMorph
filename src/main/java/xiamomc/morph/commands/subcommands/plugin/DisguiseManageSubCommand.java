package xiamomc.morph.commands.subcommands.plugin;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.commands.subcommands.plugin.management.GrantDisguiseSubCommand;
import xiamomc.morph.commands.subcommands.plugin.management.RevokeDisguiseSubCommand;
import xiamomc.pluginbase.Command.ISubCommand;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DisguiseManageSubCommand extends MorphPluginObject implements ISubCommand
{
    private final List<ISubCommand> subCommands = List.of(
            new GrantDisguiseSubCommand(),
            new RevokeDisguiseSubCommand()
    );

    @Override
    public @Nullable String getPermissionRequirement()
    {
        return "xiamomc.morph.manage";
    }

    @Override
    public List<ISubCommand> getSubCommands()
    {
        return subCommands;
    }

    @Override
    public @NotNull String getCommandName()
    {
        return "manage";
    }

    @Override
    public @Nullable String getHelpMessage()
    {
        return "管理伪装";
    }
}
