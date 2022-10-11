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
import xiamomc.morph.messages.CommandStrings;
import xiamomc.morph.messages.CommonStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
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
            {
                var convertedName = "player:" + p.getName();
                if (convertedName.toLowerCase().contains(target.toLowerCase())) list.add(convertedName);
            }
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
            commandSender.sendMessage(MessageUtils.prefixes(commandSender, CommonStrings.playerNotFoundString));
            return false;
        }

        boolean foundEntityType;
        boolean grantSuccess = false;
        String displayKey = "???";

        //检查是否为生物
        String finalTargetName = targetName;
        var avaliableType = Arrays.stream(EntityType.values())
                .filter(t -> t != EntityType.UNKNOWN && t.getKey().asString().equals(finalTargetName)).findFirst();

        foundEntityType = avaliableType.isPresent();

        if (foundEntityType)
        {
            var targetType = avaliableType.get();

            if (!targetType.isAlive() || targetType.equals(EntityType.PLAYER))
            {
                commandSender.sendMessage(MessageUtils.prefixes(commandSender, MorphStrings.invalidIdentityString));
                return true;
            }

            grantSuccess = morphs.grantMorphToPlayer(who, targetType);
            displayKey = targetType.translationKey();
        }
        else if (targetName.startsWith("player:"))
        {
            targetName = targetName.replace("player:", "");

            if (targetName.isBlank() || targetName.isEmpty())
            {
                commandSender.sendMessage(MessageUtils.prefixes(commandSender, CommonStrings.playerNotDefinedString));
                return true;
            }

            grantSuccess = morphs.grantPlayerMorphToPlayer(who, targetName);
            displayKey = targetName;
        }

        var msg = grantSuccess
                ? CommandStrings.grantSuccessString
                : CommandStrings.grantFailString;

        msg.resolve("what", Component.translatable(displayKey)).resolve("who", who.getName());

        commandSender.sendMessage(MessageUtils.prefixes(commandSender, msg));

        return true;
    }
}
