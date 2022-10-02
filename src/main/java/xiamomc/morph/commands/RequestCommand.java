package xiamomc.morph.commands;

import xiamomc.morph.MorphPlugin;
import xiamomc.morph.commands.subcommands.request.AcceptSubCommand;
import xiamomc.morph.commands.subcommands.request.DenySubCommand;
import xiamomc.morph.commands.subcommands.request.SendSubCommand;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Command.SubCommandHandler;

import java.util.List;

public class RequestCommand extends SubCommandHandler<MorphPlugin>
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
    public String getPermissionRequirement() {
        return null;
    }

    @Override
    public String getHelpMessage() {
        return "管理交换请求";
    }

    @Override
    protected String getPluginNamespace() {
        return MorphPlugin.getMorphNameSpace();
    }
}
