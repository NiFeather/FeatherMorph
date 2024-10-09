package xyz.nifeather.morph.commands;

import it.unimi.dsi.fastutil.objects.ObjectList;
import xyz.nifeather.morph.commands.subcommands.MorphSubCommandHandler;
import xyz.nifeather.morph.commands.subcommands.request.AcceptSubCommand;
import xyz.nifeather.morph.commands.subcommands.request.DenySubCommand;
import xyz.nifeather.morph.commands.subcommands.request.SendSubCommand;
import xyz.nifeather.morph.messages.HelpStrings;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.List;

public class RequestCommand extends MorphSubCommandHandler
{
    private final List<ISubCommand> subCommands = ObjectList.of(
            new SendSubCommand(),
            new AcceptSubCommand(),
            new DenySubCommand()
    );

    @Override
    public List<ISubCommand> getSubCommands()
    {
        return subCommands;
    }

    private final List<FormattableMessage> notes = ObjectList.of(
            HelpStrings.requestDescriptionSpecialNote()
    );

    @Override
    public List<FormattableMessage> getNotes()
    {
        return notes;
    }

    @Override
    public String getCommandName()
    {
        return "request";
    }

    @Override
    public String getPermissionRequirement()
    {
        return null;
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.requestDescription();
    }
}
