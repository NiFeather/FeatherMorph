package xiamomc.morph.commands.subcommands.request;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;

import java.util.ArrayList;
import java.util.List;

public class SendSubCommand extends MorphPluginObject implements ISubCommand
{
    @Resolved
    private MorphManager morphs;

    @Override
    public List<String> onTabComplete(List<String> args, CommandSender source) {
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
    public String getCommandName() {
        return "send";
    }

    @Override
    public String getPermissionRequirement() {
        return null;
    }

    @Override
    public String getHelpMessage() {
        return "向某一位玩家发送交换请求";
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
                    sourcePlayer.sendMessage(MessageUtils.prefixes(sender,
                            Component.text("未找到目标玩家")));
                    return true;
                }

                if (targetPlayer.getUniqueId().equals(sourcePlayer.getUniqueId()))
                {
                    sourcePlayer.sendMessage(MessageUtils.prefixes(sender,
                            Component.text("不能给自己发请求")));
                    return true;
                }

                if (morphs.getAvaliableDisguisesFor(sourcePlayer).stream()
                        .anyMatch(c -> c.isPlayerDisguise() && c.playerDisguiseTargetName.equals(args[0])))
                {
                    sourcePlayer.sendMessage(MessageUtils.prefixes(sender,
                            Component.text("你已经有对方的伪装形态了")));
                    return true;
                }

                morphs.createRequest(sourcePlayer, targetPlayer);
            }
            else
            {
                sender.sendMessage(MessageUtils.prefixes(sender,
                        Component.translatable("未指定请求要发给谁")));
            }
        }

        return true;
    }
}
