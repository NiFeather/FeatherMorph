package xiamomc.morph.commands;

import xiamomc.morph.MorphPlugin;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.CommandHelper;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.XiaMoJavaPlugin;

import java.util.List;

public class MorphCommandHelper extends CommandHelper<MorphPlugin>
{
    private final List<IPluginCommand> commands = List.of(
            new MorphCommand(),
            new MorphPlayerCommand(),
            new UnMorphCommand(),
            new RequestSendCommand(),
            new RequestAcceptCommand(),
            new RequestDenyCommand(),
            new MorphHelpCommand());

    @Override
    public List<IPluginCommand> getCommands() {
        return commands;
    }

    @Resolved
    private MorphPlugin plugin;

    @Override
    protected XiaMoJavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    protected String getPluginNamespace()
    {
        return MorphPlugin.getMorphNameSpace();
    }
}
