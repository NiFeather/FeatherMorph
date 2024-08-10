package xiamomc.morph.commands;

import it.unimi.dsi.fastutil.objects.ObjectList;
import org.bukkit.Bukkit;
import xiamomc.morph.MorphPlugin;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.CommandHelper;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.XiaMoJavaPlugin;

import java.util.List;
import java.util.Objects;

public class MorphCommandManager extends CommandHelper<MorphPlugin>
{
    private final List<IPluginCommand> commands = ObjectList.of(
            new MorphCommand(),
            new MorphPlayerCommand(),
            new UnMorphCommand(),
            new RequestCommand(),
            new MorphPluginCommand(),
            new AnimationCommand());

    @Override
    public boolean registerCommand(IPluginCommand command)
    {
        if (Objects.equals(command.getCommandName(), ""))
            throw new IllegalArgumentException("Trying to register a command with empty basename!");

        var cmd = Bukkit.getPluginCommand(command.getCommandName());

        if (cmd == null)
            throw new NullPointerException("'%s' doesn't have a command defined in the server.".formatted(command.getCommandName()));

        if (cmd.getExecutor().equals(this.getPlugin()))
        {
            cmd.setExecutor(command);
            cmd.setTabCompleter(new MorphTabCompleter(command));
            return true;
        }
        else
        {
            logger.warn("Ignoring command '%s' that doesn't belongs to us.".formatted(command.getCommandName()));
            return false;
        }
    }

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
