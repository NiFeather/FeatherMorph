package xiamomc.morph.commands.subcommands.request;

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
import xiamomc.pluginbase.Command.ISubCommand;

import java.util.ArrayList;
import java.util.List;

public class AcceptSubCommand extends MorphPluginObject implements ISubCommand
{
    @Resolved
    private MorphManager morphs;

    @Override
    public List<String> onTabComplete(List<String> args, CommandSender source)
    {
        var list = new ArrayList<String>();

        if (source instanceof Player player)
        {
            var reqs = morphs.getAvaliableRequestFor(player);

            reqs.forEach(r -> list.add(r.sourcePlayer.getName()));
        }

        return list;
    }

    @Override
    public String getCommandName() {
        return "accept";
    }

    @Override
    public String getPermissionRequirement() {
        return null;
    }

    @Override
    public String getHelpMessage() {
        return "接受交换请求";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (sender instanceof Player sourcePlayer)
        {
            if (args.length >= 1)
            {
                var targetPlayer = Bukkit.getPlayer(args[0]);

                if (targetPlayer == null)
                {
                    sender.sendMessage(MessageUtils.prefixes(sender,
                            Component.translatable("对方未上线或目标玩家不存在").color(NamedTextColor.RED)));
                    return true;
                }

                morphs.acceptRequest(sourcePlayer, targetPlayer);
            }
            else
            {
                sender.sendMessage(MessageUtils.prefixes(sender,
                        Component.translatable("未指定要接受谁的请求").color(NamedTextColor.RED)));
            }
        }

        return true;
    }
}
