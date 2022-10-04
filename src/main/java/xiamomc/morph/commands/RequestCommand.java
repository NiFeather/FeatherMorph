package xiamomc.morph.commands;

import xiamomc.morph.commands.subcommands.MorphSubCommandHandler;
import xiamomc.morph.commands.subcommands.request.AcceptSubCommand;
import xiamomc.morph.commands.subcommands.request.DenySubCommand;
import xiamomc.morph.commands.subcommands.request.SendSubCommand;
import xiamomc.pluginbase.Command.ISubCommand;

import java.util.List;

public class RequestCommand extends MorphSubCommandHandler
{
    private final List<ISubCommand> subCommands = List.of(
            new SendSubCommand(),
            new AcceptSubCommand(),
            new DenySubCommand()
    );

    @Override
    public List<ISubCommand> getSubCommands()
    {
        return subCommands;
    }

    private final List<String> notes = List.of(
            "交换请求接受后双方都可以变成对方的样子"
    );

    @Override
    public List<String> getNotes()
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
    public String getHelpMessage()
    {
        return "管理交换请求";
    }
}
