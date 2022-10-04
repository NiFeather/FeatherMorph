package xiamomc.morph.commands;

import me.libraryaddict.disguise.DisguiseAPI;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.IPluginCommand;

import java.util.ArrayList;
import java.util.List;

public class MorphCommand extends MorphPluginObject implements IPluginCommand {
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
                        //目标类型
                        var type = EntityType.valueOf(keyAsEntityTypeFormat);

                        //玩家正在看的实体
                        var targetEntity = player.getTargetEntity(5);

                        //是否应该复制伪装
                        var shouldCopy = false;

                        //如果实体有伪装，则检查实体的伪装类型
                        if (DisguiseAPI.isDisguised(targetEntity))
                        {
                            var disg = DisguiseAPI.getDisguise(targetEntity);
                            shouldCopy = disg.getType().getEntityType().equals(type);
                        }

                        if (shouldCopy)
                            morphManager.morphCopy(player, targetEntity); //如果应该复制伪装，则复制给玩家
                        else if (targetEntity != null && targetEntity.getType().equals(type) && !DisguiseAPI.isDisguised(targetEntity))
                            morphManager.morphEntity(player, targetEntity); //否则，如果目标实体是我们想要的实体，则伪装成目标实体
                        else
                            morphManager.morphEntityType(player, type); //否则，只简单地创建实体伪装

                        var msg = Component.translatable("成功伪装为")
                                .append(Component.translatable(type.translationKey()))
                                .append(Component.text("！"));
                        sender.sendMessage(MessageUtils.prefixes(msg));

                        return true;
                    }
                    catch (IllegalArgumentException iae)
                    {
                        sender.sendMessage(MessageUtils.prefixes(Component.translatable("未能解析" + args[0], TextColor.color(255, 0, 0))));
                    }
                }
                else
                {
                    sender.sendMessage(MessageUtils.prefixes(Component.translatable("你尚未拥有此伪装")));
                }
            }
            else
                sender.sendMessage(MessageUtils.prefixes(Component.text("你需要指定要伪装的对象")));
        }

        return true;
    }

    @Override
    public String getCommandName() {
        return "morph";
    }

    @Resolved
    private MorphManager morphs;

    @Override
    public List<String> onTabComplete(List<String> args, CommandSender source)
    {
        var list = new ArrayList<String>();

        if (args.size() > 1) return list;

        if (source instanceof Player player) {
            //Logger.warn("BUFFERS: " + Arrays.toString(buffers));

            var arg = args.get(0);

            var infos = morphs.getAvaliableDisguisesFor(player)
                    .stream().filter(c -> !c.isPlayerDisguise()).toList();

            for (var di : infos) {
                var name = di.type.getKey().asString();
                if (!name.toLowerCase().contains(arg.toLowerCase())) continue;

                list.add(name);
            }
        }

        return list;
    }

    @Override
    public String getPermissionRequirement() {
        return null;
    }

    @Override
    public String getHelpMessage() {
        return "伪装成某种生物";
    }
}
