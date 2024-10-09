package xyz.nifeather.morph.commands.subcommands.plugin.management;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xiamomc.morph.messages.*;
import xyz.nifeather.morph.messages.*;
import xyz.nifeather.morph.misc.DisguiseTypes;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.List;

public class GrantDisguiseSubCommand extends MorphPluginObject implements ISubCommand
{
    @Override
    public @NotNull String getCommandName()
    {
        return "grant";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.manageGrantDescription();
    }

    @Override
    public @Nullable String getPermissionRequirement()
    {
        return "xiamomc.morph.manage.grant";
    }

    @Override
    public List<String> onTabComplete(List<String> args, CommandSender source)
    {
        var list = new ObjectArrayList<String>();
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
            var targetLowerCase = target.toLowerCase();

            for (var p : MorphManager.getProviders())
            {
                if (p == MorphManager.fallbackProvider) continue;

                var ns = p.getNameSpace();
                p.getAllAvailableDisguises().forEach(s ->
                {
                    var str = ns + ":" + s;
                    if (str.toLowerCase().contains(targetLowerCase)) list.add(str);
                });

                list.add(ns + ":" + "@all");
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

        var who = Bukkit.getPlayerExact(strings[0]);
        var targetName = strings[1];

        if (who == null || !who.isOnline())
        {
            commandSender.sendMessage(MessageUtils.prefixes(commandSender, CommonStrings.playerNotFoundString()));
            return false;
        }

        if (!targetName.contains(":"))
            targetName = "minecraft:" + targetName;

        //检查是否已知
        var provider = MorphManager.getProvider(targetName);

        var nameType = DisguiseTypes.fromId(targetName);
        if (nameType.toStrippedId(targetName).equals("@all"))
        {
            var allDisg = provider.getAllAvailableDisguises();
            allDisg.forEach(id -> grantDisguise(who, nameType.toId(id), commandSender));

            return true;
        }
        else if (!provider.isValid(targetName))
        {
            commandSender.sendMessage(MessageUtils.prefixes(commandSender, MorphStrings.invalidIdentityString()));
            return true;
        }

        grantDisguise(who, targetName, commandSender);

        return true;
    }

    private void grantDisguise(Player who, String targetName, CommandSender commandSender)
    {
        var msg = morphs.grantMorphToPlayer(who, targetName)
                ? CommandStrings.grantSuccessString()
                : CommandStrings.grantFailString();

        msg.resolve("what", Component.text(targetName)).resolve("who", who.getName());

        commandSender.sendMessage(MessageUtils.prefixes(commandSender, msg));

    }
}
