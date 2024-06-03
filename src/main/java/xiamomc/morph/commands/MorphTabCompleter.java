package xiamomc.morph.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Command.IPluginCommand;

import java.util.Arrays;
import java.util.List;

public class MorphTabCompleter implements TabCompleter
{
    private final IPluginCommand command;

    public MorphTabCompleter(IPluginCommand command)
    {
        this.command = command;
    }

    /**
     * Requests a list of possible completions for a command argument.
     *
     * @param sender  Source of the command.  For players tab-completing a
     *                command inside of a command block, this will be the player, not
     *                the command block.
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    The arguments passed to the command, including final
     *                partial argument to be completed
     * @return A List of possible completions for the final argument, or null
     * to default to the command executor
     */
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        return this.command.onTabComplete(Arrays.stream(args).toList(), sender);
    }
}
