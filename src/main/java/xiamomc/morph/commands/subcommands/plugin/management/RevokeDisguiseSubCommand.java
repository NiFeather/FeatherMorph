package xiamomc.morph.commands.subcommands.plugin.management;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.messages.CommandStrings;
import xiamomc.morph.messages.CommonStrings;
import xiamomc.morph.messages.HelpStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.messages.FormattableMessage;

import java.util.ArrayList;
import java.util.List;

public class RevokeDisguiseSubCommand extends MorphPluginObject implements ISubCommand
{
    @Override
    public @NotNull String getCommandName()
    {
        return "revoke";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.manageRevokeDescription();
    }

    @Override
    public @Nullable String getPermissionRequirement()
    {
        return "xiamomc.morph.manage.revoke";
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
        else if (args.size() == 2) //size == 2: 补全拥有的伪装
        {
            var player = Bukkit.getPlayer(name);

            if (player != null)
            {
                var disguises = morphs.getAvaliableDisguisesFor(player);

                for (var d : disguises)
                {
                    if (d.getKey().toLowerCase().contains(target.toLowerCase()))
                    {
                        list.add(d.getKey());
                    }
                }
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
            commandSender.sendMessage(MessageUtils.prefixes(commandSender, CommonStrings.playerNotFoundString()));
            return false;
        }

        var info = morphs.getAvaliableDisguisesFor(who)
                .stream().filter(i -> i.getKey().equals(targetName)).findFirst().orElse(null);

        var revokeSuccess = info != null && morphs.revokeMorphFromPlayer(who, info.getKey());

        var msg = revokeSuccess
                ? CommandStrings.revokeSuccessString()
                : CommandStrings.revokeFailString();

        msg.resolve("what", Component.translatable(targetName)).resolve("who", who.getName());

        commandSender.sendMessage(MessageUtils.prefixes(commandSender, msg));

        return true;
    }
}
