package xiamomc.morph.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphUtils;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.PluginObject;

public class DisguiseTestCommand extends PluginObject implements IPluginCommand {
    @Resolved
    private MorphUtils morphUtils;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (sender instanceof Player player)
        {
            var targetEntity = player.getTargetEntity(3);

            if (targetEntity != null) morphUtils.morph(player, targetEntity);
        }

        return true;
    }

    @Override
    public String getCommandName() {
        return "testdisg";
    }
}
