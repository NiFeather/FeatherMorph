package xiamomc.morph.commands;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
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

public class MorphCommand extends MorphPluginObject implements IPluginCommand
{
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

                var infoOptional = morphManager.getAvaliableDisguisesFor(player).stream()
                        .filter(i -> i.getKey().equals(key)).findFirst();

                if (infoOptional.isPresent())
                {
                    try
                    {
                        //获取到的伪装
                        var info = infoOptional.get();

                        //目标类型
                        var type = info.type;

                        if (!type.isAlive())
                        {
                            sender.sendMessage(MessageUtils.prefixes(sender,
                                    Component.translatable("此ID不能被用于伪装" , TextColor.color(255, 0, 0))));

                            return true;
                        }

                        //玩家正在看的实体
                        var targetEntity = player.getTargetEntity(5);

                        //是否应该复制伪装
                        var shouldCopy = false;

                        //如果实体有伪装，则检查实体的伪装类型
                        if (DisguiseAPI.isDisguised(targetEntity))
                        {
                            var disg = DisguiseAPI.getDisguise(targetEntity);
                            assert disg != null;

                            shouldCopy = info.isPlayerDisguise()
                                    ? disg.isPlayerDisguise() && ((PlayerDisguise) disg).getName().equals(info.playerDisguiseTargetName)
                                    : disg.getType().getEntityType().equals(type);
                        }

                        if (info.isPlayerDisguise())
                        {
                            if (shouldCopy)
                                morphManager.morphCopy(player, targetEntity); //如果应该复制伪装，则复制给玩家
                            else if (targetEntity instanceof Player targetPlayer && targetPlayer.getName().equals(info.playerDisguiseTargetName) && !DisguiseAPI.isDisguised(targetEntity))
                                morphManager.morphEntity(player, targetPlayer); //否则，如果目标实体是我们想要的玩家，则伪装成目标实体
                            else
                                morphManager.morphPlayer(player, info.playerDisguiseTargetName); //否则，只简单地创建玩家伪装
                        }
                        else
                        {
                            if (shouldCopy)
                                morphManager.morphCopy(player, targetEntity); //如果应该复制伪装，则复制给玩家
                            else if (targetEntity != null && targetEntity.getType().equals(type) && !DisguiseAPI.isDisguised(targetEntity))
                                morphManager.morphEntity(player, targetEntity); //否则，如果目标实体是我们想要的实体，则伪装成目标实体
                            else
                                morphManager.morphEntityType(player, type); //否则，只简单地创建实体伪装
                        }

                        var msg = Component.translatable("成功伪装为")
                                .append(Component.translatable(type.translationKey()))
                                .append(Component.text("！"));
                        sender.sendMessage(MessageUtils.prefixes(sender, msg));

                        return true;
                    }
                    catch (IllegalArgumentException iae)
                    {
                        sender.sendMessage(MessageUtils.prefixes(sender,
                                Component.translatable("未能解析" + args[0], TextColor.color(255, 0, 0))));
                    }
                }
                else
                {
                    sender.sendMessage(MessageUtils.prefixes(sender,
                            Component.translatable("你尚未拥有此伪装")));
                }
            }
            else
                sender.sendMessage(MessageUtils.prefixes(sender,
                        Component.text("你需要指定要伪装的对象")));
        }

        return true;
    }

    @Override
    public String getCommandName()
    {
        return "morph";
    }

    @Resolved
    private MorphManager morphs;

    @Override
    public List<String> onTabComplete(List<String> args, CommandSender source)
    {
        var list = new ArrayList<String>();

        if (args.size() > 1) return list;

        if (source instanceof Player player)
        {
            //Logger.warn("BUFFERS: " + Arrays.toString(buffers));

            var arg = args.get(0);

            var infos = morphs.getAvaliableDisguisesFor(player);

            for (var di : infos)
            {
                var name = di.getKey();
                if (!name.toLowerCase().contains(arg.toLowerCase())) continue;

                list.add(name);
            }
        }

        return list;
    }

    @Override
    public String getPermissionRequirement()
    {
        return null;
    }

    @Override
    public String getHelpMessage()
    {
        return "伪装成某种生物";
    }
}
