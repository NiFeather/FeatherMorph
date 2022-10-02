package xiamomc.morph.commands.subcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ISubCommand
{
    public List<String> onTabComplete(String[] args, CommandSender source);

    public String getSubCommandName();

    public String getPermissionRequirement();

    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args);
}
