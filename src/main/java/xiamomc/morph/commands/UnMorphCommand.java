package xiamomc.morph.commands;

import me.libraryaddict.disguise.DisguiseAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.MessageUtils;
import xiamomc.pluginbase.Command.IPluginCommand;

import java.util.List;

public class UnMorphCommand  extends MorphPluginObject implements IPluginCommand {
    @Override
    public String getCommandName() {
        return "unmorph";
    }

    @Override
    public List<String> onTabComplete(String baseName, String[] args, CommandSender source) {
        return List.of("");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player)
        {
            if (DisguiseAPI.isDisguised(player))
            {
                var disguise = DisguiseAPI.getDisguise(player);
                disguise.removeDisguise(player);

                player.sendMessage(MessageUtils.prefixes(Component.text("已取消伪装")));
            }
        }
        return true;
    }
}
