package xiamomc.morph.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.IPluginCommand;

import java.util.List;

public class UnMorphCommand  extends MorphPluginObject implements IPluginCommand {
    @Override
    public String getCommandName() {
        return "unmorph";
    }

    @Override
    public List<String> onTabComplete(List<String> list, CommandSender commandSender) {
        return null;
    }

    @Override
    public String getPermissionRequirement() {
        return null;
    }

    @Override
    public String getHelpMessage() {
        return "取消自己的伪装";
    }

    @Resolved
    private MorphManager morphs;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player)
        {
            morphs.unMorph(player);
        }
        return true;
    }
}
