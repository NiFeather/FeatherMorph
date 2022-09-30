package xiamomc.morph.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.PluginObject;

public class TestPlayerCommand extends PluginObject implements IPluginCommand {
    @Resolved
    private MorphUtils morphUtils;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (sender instanceof Player player)
        {
            morphUtils.morph(player, EntityType.PLAYER);
        }

        return true;
    }

    @Override
    public String getCommandName() {
        return "testplayer";
    }
}
