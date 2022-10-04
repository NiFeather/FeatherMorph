package xiamomc.morph.commands;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.DisguiseInfo;
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

                    sender.sendMessage(MessageUtils.prefixes(sender, msg));

                    return true;
                }

                var shouldCopy = false;

                //如果实体有伪装，则检查伪装是否是我们想要的类型
                if (DisguiseAPI.isDisguised(targetEntity) && DisguiseAPI.getDisguise(targetEntity).isPlayerDisguise())
                {
                    var disg = (PlayerDisguise)DisguiseAPI.getDisguise(targetEntity);
                    shouldCopy = disg.getName().equals(targetName);
                }

                if (shouldCopy)
                    morphManager.morphCopy(sourcePlayer, targetEntity); //如果应该复制伪装，则复制给玩家
                else if (targetEntity instanceof Player targetPlayer && targetPlayer.getName().equals(targetName) && !DisguiseAPI.isDisguised(targetEntity))
                    morphManager.morphEntity(sourcePlayer, targetPlayer); //否则，如果目标实体是我们想要的玩家，则伪装成目标实体
                else
                    morphManager.morphPlayer(sourcePlayer, args[0]); //否则，只简单地创建玩家伪装

                var msg = Component.translatable("成功伪装为")
                        .append(Component.text(args[0] + "！"));

                sender.sendMessage(MessageUtils.prefixes(sender, msg));
            }
            else
                sender.sendMessage(MessageUtils.prefixes(sender, Component.translatable("你需要指定要伪装的对象")));
        }

        return true;
    }

    @Override
    public String getCommandName() {
        return "morphplayer";
    }

    @Override
    public List<String> onTabComplete(List<String> args, CommandSender source)
    {
        var list = new ArrayList<String>();

        if (args.size() > 1) return list;

        if (source instanceof Player player)
        {
            var arg = args.get(0).toLowerCase();

            var infos = morphManager.getAvaliableDisguisesFor(player)
                    .stream().filter(DisguiseInfo::isPlayerDisguise).toList();

            for (var di : infos) {
                var name = di.playerDisguiseTargetName;
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
        return "伪装为某一玩家";
    }
}
