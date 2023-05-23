package xiamomc.morph.commands;

import it.unimi.dsi.fastutil.objects.ObjectList;
import xiamomc.morph.MorphPlugin;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.CommandHelper;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.XiaMoJavaPlugin;

import java.util.List;

public class MorphCommandManager extends CommandHelper<MorphPlugin>
{
    private final List<IPluginCommand> commands = ObjectList.of(
            new MorphCommand(),
            new MorphPlayerCommand(),
            new UnMorphCommand(),
            new RequestCommand(),
            new MorphPluginCommand());

    @Override
    public List<IPluginCommand> getCommands()
    {
        return commands;
    }

    @Resolved
    private MorphPlugin plugin;

    @Override
    protected XiaMoJavaPlugin getPlugin()
    {
        return plugin;
    }

    @Override
    protected String getPluginNamespace()
    {
        return MorphPlugin.getMorphNameSpace();
    }
}
