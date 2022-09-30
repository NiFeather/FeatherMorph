package xiamomc.morph.commands;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.PluginObject;

public class DisguiseTestCommand extends PluginObject implements IPluginCommand {
    @Resolved
    private MorphManager morphManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (sender instanceof Player player)
        {
            if (args.length >= 1)
            {
                var key = args[0];
                var keyAsEntityTypeFormat = key.replace(Key.MINECRAFT_NAMESPACE + ":", "").toUpperCase();

                if (morphManager.getAvaliableDisguisesFor(player).stream()
                        .anyMatch(i -> i.type.toString().equals(keyAsEntityTypeFormat)))
                {
                    try
                    {
                        var type = EntityType.valueOf(keyAsEntityTypeFormat);

                        var targetEntity = player.getTargetEntity(3);

                        if (targetEntity != null && targetEntity.getType().equals(type))
                            morphManager.morph(player, targetEntity);
                        else
                            morphManager.morph(player, type);

                        sender.sendMessage(Component.translatable("成功伪装成")
                                .append(Component.translatable(type.translationKey())));

                        return true;
                    }
                    catch (IllegalArgumentException iae)
                    {
                        sender.sendMessage(Component.translatable("未能解析" + args[0], TextColor.color(255, 0, 0)));
                    }
                }
                else
                {
                    sender.sendMessage(Component.translatable("你尚未拥有此伪装"));
                }
            }
            else
                sender.sendMessage(Component.text("你需要指定要伪装的对象"));
        }

        return true;
    }

    @Override
    public String getCommandName() {
        return "morph";
    }
}
