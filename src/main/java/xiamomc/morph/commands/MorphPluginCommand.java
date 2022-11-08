package xiamomc.morph.commands;

import it.unimi.dsi.fastutil.objects.ObjectList;
import xiamomc.morph.commands.subcommands.MorphSubCommandHandler;
import xiamomc.morph.commands.subcommands.plugin.*;
import xiamomc.morph.messages.HelpStrings;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.messages.FormattableMessage;

import java.util.List;

public class MorphPluginCommand extends MorphSubCommandHandler
{
    @Override
    public String getCommandName()
    {
        return "mmorph";
    }

    @Override
    public String getPermissionRequirement()
    {
        return null;
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.mmorphDescription();
    }

    private final List<ISubCommand> subCommands = ObjectList.of(
            new ReloadSubCommand(),
            new HelpSubCommand(),
            new ToggleSelfSubCommand(),
            new QuerySubCommand(),
            new QueryAllSubCommand(),
            new DisguiseManageSubCommand(),
            new OptionSubCommand()
    );

    @Override
    public List<ISubCommand> getSubCommands()
    {
        return subCommands;
    }

    private final List<FormattableMessage> notes = List.of();

    @Override
    public List<FormattableMessage> getNotes()
    {
        return notes;
    }
}
