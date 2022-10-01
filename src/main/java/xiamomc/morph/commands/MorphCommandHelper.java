package xiamomc.morph.commands;

import xiamomc.morph.MorphPlugin;
import xiamomc.pluginbase.Command.CommandHelper;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.XiaMoJavaPlugin;

import java.util.List;

public class MorphCommandHelper extends CommandHelper
{
    private final List<IPluginCommand> commands = List.of(
            new MorphCommand(),
            new MorphPlayerCommand(),
            new UnMorphCommand(),
            new RequestSendCommand(),
            new RequestAcceptCommand(),
            new RequestDenyCommand());

    @Override
    public List<IPluginCommand> getCommands() {
        return commands;
    }

    @Override
    protected XiaMoJavaPlugin getPlugin() {
        return MorphPlugin.GetInstance();
    }
}
