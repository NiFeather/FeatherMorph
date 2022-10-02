package xiamomc.morph.commands;

import xiamomc.morph.MorphPlugin;
import xiamomc.morph.commands.subcommands.plugin.HelpSubCommand;
import xiamomc.morph.commands.subcommands.ISubCommand;
import xiamomc.morph.commands.subcommands.plugin.ReloadSubCommand;
import xiamomc.morph.commands.subcommands.SubCommandHelper;
import xiamomc.morph.commands.subcommands.plugin.ToggleSelfSubCommand;

import java.util.List;

public class MorphPluginCommand extends SubCommandHelper<MorphPlugin> {
    @Override
    public String getCommandName() {
        return "mmorph";
    }

    private final List<ISubCommand> subCommands = List.of(
            new ReloadSubCommand(),
            new HelpSubCommand(),
            new ToggleSelfSubCommand()
    );

    @Override
    protected List<ISubCommand> getSubCommands() {
        return subCommands;
    }

    @Override
    protected String getPluginNamespace() {
        return MorphPlugin.getMorphNameSpace();
    }
}
