package xiamomc.morph.commands.subcommands.request;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.commands.subcommands.ISubCommand;
import xiamomc.morph.misc.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SendSubCommand extends MorphPluginObject implements ISubCommand
{
    @Resolved
    private MorphManager morphs;

    @Override
    public List<String> onTabComplete(String[] args, CommandSender source) {
        var list = new ArrayList<String>();

        if (source instanceof Player player)
        {
            Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.getUniqueId().equals(player.getUniqueId()))
                    .forEach(p -> list.add(p.getName()));
        }

        return list;
    }

    @Override
    public String getSubCommandName() {
        return "send";
    }

    @Override
    public String getPermissionRequirement() {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (sender instanceof Player sourcePlayer)
        {
            if (args.length >= 1)
            {
                var targetPlayer = Bukkit.getPlayer(args[0]);

                if (targetPlayer == null)
                {
                    sourcePlayer.sendMessage(MessageUtils.prefixes(Component.text("未找到目标玩家")));
                    return true;
                }

                if (targetPlayer.getUniqueId().equals(sourcePlayer.getUniqueId()))
                {
                    sourcePlayer.sendMessage(MessageUtils.prefixes(Component.text("不能给自己发请求")));
                    return true;
                }

                if (morphs.getAvaliableDisguisesFor(sourcePlayer).stream()
                        .anyMatch(c -> c.isPlayerDisguise && c.playerDisguiseTargetName.equals(args[0])))
                {
                    sourcePlayer.sendMessage(MessageUtils.prefixes(Component.text("你已经有对方的伪装形态了")));
                    return true;
                }

                morphs.createRequest(sourcePlayer, targetPlayer);
            }
            else
            {
                sender.sendMessage(MessageUtils.prefixes(Component.translatable("未指定请求要发给谁")));
            }
        }

        return true;
    }
}
