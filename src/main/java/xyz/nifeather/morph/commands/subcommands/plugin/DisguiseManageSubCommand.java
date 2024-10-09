package xyz.nifeather.morph.commands.subcommands.plugin;

import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.commands.subcommands.plugin.management.ForceMorphSubCommand;
import xyz.nifeather.morph.commands.subcommands.plugin.management.ForceUnmorphSubCommand;
import xyz.nifeather.morph.commands.subcommands.plugin.management.GrantDisguiseSubCommand;
import xyz.nifeather.morph.commands.subcommands.plugin.management.RevokeDisguiseSubCommand;
import xyz.nifeather.morph.messages.HelpStrings;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

import java.util.List;

public class DisguiseManageSubCommand extends MorphPluginObject implements ISubCommand
{
    private final List<ISubCommand> subCommands = ObjectList.of(
            new GrantDisguiseSubCommand(),
            new RevokeDisguiseSubCommand(),
            new ForceUnmorphSubCommand(),
            new ForceMorphSubCommand()
    );

    @Override
    public @Nullable String getPermissionRequirement()
    {
        return CommonPermissions.MANAGE_DISGUISES;
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
