package xiamomc.morph.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.IPluginCommand;

import java.util.ArrayList;
import java.util.List;

public class MorphPlayerCommand extends MorphPluginObject implements IPluginCommand {
    @Resolved
    private MorphManager morphManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (sender instanceof Player sourcePlayer)
        {
            if (args.length >= 1)
            {
                var targetName = args[0];
                var targetEntity = sourcePlayer.getTargetEntity(5);

                var avaliable = morphManager.getAvaliableDisguisesFor(sourcePlayer).stream()
                        .filter(i -> targetName.equals(i.playerDisguiseTargetName)).findFirst();

                if (!avaliable.isPresent())
                {
                    var msg = Component.translatable("你尚未拥有")
                            .append(Component.text(args[0]))
                            .append(Component.translatable("的伪装"));

                    sender.sendMessage(MessageUtils.prefixes(msg));

                    return true;
                }

                if (targetEntity instanceof Player targetPlayer && targetPlayer.getName().equals(targetName))
                    morphManager.morph(sourcePlayer, targetPlayer);
                else
                    morphManager.morph(sourcePlayer, args[0]);

                var msg = Component.translatable("成功伪装成")
                        .append(Component.text(args[0] + "!"));

                sender.sendMessage(MessageUtils.prefixes(msg));
            }
            else
                sender.sendMessage(MessageUtils.prefixes(Component.translatable("你需要指定要伪装的对象")));
        }

        return true;
    }

    @Override
    public String getCommandName() {
        return "morphplayer";
    }

    @Override
    public List<String> onTabComplete(String baseName, String[] args, CommandSender source)
    {
        var list = new ArrayList<String>();

        if (source instanceof Player player)
        {
            var arg = args[0];

            var infos = morphManager.getAvaliableDisguisesFor(player)
                    .stream().filter(c -> c.isPlayerDisguise).toList();

            for (var di : infos) {
                var name = di.playerDisguiseTargetName;
                if (!name.contains(arg)) continue;

                list.add(name);
            }
        }

        return list;
    }
}
