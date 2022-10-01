package xiamomc.morph.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.PluginObject;

import java.util.List;

public class RequestAcceptCommand extends MorphPluginObject implements IPluginCommand {
    @Override
    public String getCommandName() {
        return "acceptrequest";
    }

    @Override
    public List<String> onTabComplete(String baseName, String[] args, CommandSender source) {
        return List.of("");
    }

    @Resolved
    private MorphManager morphs;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (sender instanceof Player sourcePlayer)
        {
            if (args.length >= 1)
            {
                var targetPlayer = Bukkit.getPlayer(args[0]);

                if (targetPlayer == null)
                {
                    //todo
                    return true;
                }

                morphs.acceptRequest(sourcePlayer, targetPlayer);
            }
            else
            {
                //todo
            }
        }

        return true;
    }
}
