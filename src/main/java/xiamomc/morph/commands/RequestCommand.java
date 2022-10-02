package xiamomc.morph.commands;

import xiamomc.morph.MorphPlugin;
import xiamomc.morph.commands.subcommands.ISubCommand;
import xiamomc.morph.commands.subcommands.SubCommandHelper;
import xiamomc.morph.commands.subcommands.request.AcceptSubCommand;
import xiamomc.morph.commands.subcommands.request.DenySubCommand;
import xiamomc.morph.commands.subcommands.request.SendSubCommand;

import java.util.List;

public class RequestCommand extends SubCommandHelper<MorphPlugin>
{
    private final List<ISubCommand> subCommands = List.of(
            new SendSubCommand(),
            new AcceptSubCommand(),
            new DenySubCommand()
    );

    @Override
    protected List<ISubCommand> getSubCommands() {
        return subCommands;
    }

    @Override
    public String getCommandName() {
        return "request";
    }

    @Override
    protected String getPluginNamespace() {
        return MorphPlugin.getMorphNameSpace();
    }
}
