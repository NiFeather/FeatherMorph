package xiamomc.morph.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.PluginObject;

import java.util.ArrayList;
import java.util.List;

public class RequestDenyCommand extends MorphPluginObject implements IPluginCommand {
    @Override
    public String getCommandName() {
        return "denyrequest";
    }

    @Override
    public List<String> onTabComplete(String baseName, String[] args, CommandSender source) {
        var list = new ArrayList<String>();

        if (source instanceof Player player)
        {
            var reqs = morphs.getAvaliableRequestFor(player);

            reqs.forEach(r -> list.add(r.sourcePlayer.getName()));
        }

        return list;
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
                    sender.sendMessage(MessageUtils.prefixes(Component.translatable("对方未上线或目标玩家不存在").color(NamedTextColor.RED)));
                    return true;
                }

                morphs.denyRequest(sourcePlayer, targetPlayer);
            }
            else
            {
                sender.sendMessage(MessageUtils.prefixes(Component.translatable("未指定要拒绝谁的请求").color(NamedTextColor.RED)));
            }
        }

        return true;
    }
}
