package xiamomc.morph.commands;

import me.libraryaddict.disguise.DisguiseAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphPluginObject;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.PluginObject;

public class UnMorphCommand  extends MorphPluginObject implements IPluginCommand {
    @Override
    public String getCommandName() {
        return "unmorph";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player)
        {
            if (DisguiseAPI.isDisguised(player))
            {
                var disguise = DisguiseAPI.getDisguise(player);
                disguise.removeDisguise(player);

                player.sendMessage(Component.text("成功取消伪装"));
            }
        }
        return true;
    }
}
