package xiamomc.morph.commands.subcommands.plugin;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.commands.subcommands.plugin.management.ForceUnmorphSubCommand;
import xiamomc.morph.commands.subcommands.plugin.management.GrantDisguiseSubCommand;
import xiamomc.morph.commands.subcommands.plugin.management.RevokeDisguiseSubCommand;
import xiamomc.morph.messages.HelpStrings;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.messages.FormattableMessage;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DisguiseManageSubCommand extends MorphPluginObject implements ISubCommand
{
    private final List<ISubCommand> subCommands = List.of(
            new GrantDisguiseSubCommand(),
            new RevokeDisguiseSubCommand(),
            new ForceUnmorphSubCommand()
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
    public @Nullable FormattableMessage getHelpMessage()
    {
        return HelpStrings.manageDescription();
    }
}
