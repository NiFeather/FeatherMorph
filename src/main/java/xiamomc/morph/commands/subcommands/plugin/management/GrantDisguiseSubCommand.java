package xiamomc.morph.commands.subcommands.plugin.management;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GrantDisguiseSubCommand extends MorphPluginObject implements ISubCommand
{
    @Override
    public @NotNull String getCommandName()
    {
        return "grant";
    }

    @Override
    public @Nullable String getHelpMessage()
    {
        return "授予某个人某个伪装";
    }

    @Override
    public @Nullable String getPermissionRequirement()
    {
        return "xiamomc.morph.manage.grant";
    }

    @Override
    public List<String> onTabComplete(List<String> args, CommandSender source)
    {
        var list = new ArrayList<String>();
        if (args.size() > 2) return list;

        var name = args.size() >= 1 ? args.get(0) : "";
        var target = args.size() == 2 ? args.get(1) : "";

        var onlinePlayers = Bukkit.getOnlinePlayers();

        if (args.size() == 1) //size == 1: 补全玩家
        {
            for (var p : onlinePlayers)
                if (p.getName().toLowerCase().contains(name.toLowerCase())) list.add(p.getName());
        }
        else if (args.size() == 2) //size == 2: 补全生物和玩家
        {
            //生物
            for (var eT : EntityType.values())
            {
                if (eT == EntityType.UNKNOWN) continue;

                var key = eT.getKey().asString();
                if (key.toLowerCase().contains(target.toLowerCase())) list.add(key);
            }

            //玩家
            for (var p : onlinePlayers)
                if (p.getName().toLowerCase().contains(target.toLowerCase())) list.add(p.getName());
        }

        return list;
    }

    @Resolved
    private MorphManager morphs;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull String[] strings)
    {
        if (strings.length != 2) return false;

        var who = Bukkit.getPlayer(strings[0]);
        var targetName = strings[1];

        if (who == null || !who.isOnline())
        {
            commandSender.sendMessage(MessageUtils.prefixes(commandSender, Component.text("目标玩家不存在或已离线")));
            return false;
        }

        boolean foundEntityType;
        boolean foundPlayer;
        boolean grantSuccess = false;
        String displayKey = "???";

        //检查是否为生物
        var avaliableType = Arrays.stream(EntityType.values())
                .filter(t -> t != EntityType.UNKNOWN && t.getKey().asString().equals(targetName)).findFirst();

        var avaliablePlayer = Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getName().equals(targetName)).findFirst();

        foundEntityType = avaliableType.isPresent();
        foundPlayer = avaliablePlayer.isPresent();

        if (foundEntityType)
        {
            var targetType = avaliableType.get();

            if (!targetType.isAlive())
            {
                commandSender.sendMessage(MessageUtils.prefixes(commandSender, Component.text("无效的生物ID")));
                return true;
            }

            grantSuccess = morphs.grantMorphToPlayer(who, targetType);
            displayKey = targetType.translationKey();
        }
        else if (foundPlayer)
        {
            grantSuccess = morphs.grantPlayerMorphToPlayer(who, targetName);
            displayKey = targetName;
        }

        if (grantSuccess)
            commandSender.sendMessage(MessageUtils.prefixes(commandSender,
                 Component.text("成功将")
                        .append(Component.translatable(displayKey))
                        .append(Component.text("的伪装给与"))
                        .append(Component.text(who.getName()))
                         .color(NamedTextColor.GREEN)));
        else
            commandSender.sendMessage(MessageUtils.prefixes(commandSender,
                    Component.text("未能将")
                            .append(Component.translatable(displayKey))
                            .append(Component.text("的伪装给与"))
                            .append(Component.text(who.getName()))
                            .append(Component.text("他是否已经拥有此伪装？"))
                            .color(NamedTextColor.RED)));
        return true;
    }
}
