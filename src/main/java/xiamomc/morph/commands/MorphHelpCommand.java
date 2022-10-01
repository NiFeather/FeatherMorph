package xiamomc.morph.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.MessageUtils;
import xiamomc.pluginbase.Command.IPluginCommand;

import java.util.List;

public class MorphHelpCommand extends MorphPluginObject implements IPluginCommand {
    @Override
    public String getCommandName() {
        return "morphhelp";
    }

    @Override
    public List<String> onTabComplete(String baseName, String[] args, CommandSender source) {
        return null;
    }

    private final String[] helpMessages = new String[]
            {
                    "Morph功能指引：",
                    "-----------------------------",
                    "/morph <id>: 将你伪装成某一个生物",
                    "/morphplayer <玩家名>: 将你伪装成某一位玩家",
                    "/unmorph: 取消伪装",
                    "\u00a7l伪装可以通过击杀生物获得",
                    "\u00a7l伪装时会优先复制视线方向5格以内的相同生物或玩家进行伪装",
                    "-----------------------------",
                    "/sendrequest <玩家名>: 向某一位玩家发送交换请求",
                    "/acceptrequest <玩家名>: 接受交换",
                    "/denyrequest <玩家名>: 拒绝交换",
                    "\u00a7l接受交换请求后将允许双方变成对方的样子",
                    "-----------------------------",
            };

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        for (var s : helpMessages)
        {
            sender.sendMessage(MessageUtils.prefixes(Component.text(s)));
        }
        return true;
    }
}
