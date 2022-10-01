package xiamomc.morph.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.PluginObject;

public class MorphPlayerCommand extends PluginObject implements IPluginCommand {
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
                var targetEntity = sourcePlayer.getTargetEntity(3);

                var avaliable = morphManager.getAvaliableDisguisesFor(sourcePlayer).stream()
                        .filter(i -> targetName.equals(i.playerDisguiseTargetName)).findFirst();

                if (!avaliable.isPresent())
                {
                    sender.sendMessage(Component.translatable("你尚未拥有")
                            .append(Component.text(args[0]))
                            .append(Component.translatable("的伪装")));

                    return true;
                }

                if (targetEntity instanceof Player targetPlayer && targetPlayer.getName().equals(targetName))
                    morphManager.morph(sourcePlayer, targetPlayer);
                else
                    morphManager.morph(sourcePlayer, args[0]);

                sender.sendMessage(Component.translatable("成功伪装成")
                        .append(Component.text(args[0] + "!")));
            }
            else
                sender.sendMessage(Component.translatable("你需要指定要伪装的对象"));
        }

        return true;
    }

    @Override
    public String getCommandName() {
        return "morphplayer";
    }
}
