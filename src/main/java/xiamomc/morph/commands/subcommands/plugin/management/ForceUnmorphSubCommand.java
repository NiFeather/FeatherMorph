package xiamomc.morph.commands.subcommands.plugin.management;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;

import java.util.Objects;

public class ForceUnmorphSubCommand extends MorphPluginObject implements ISubCommand
{
    @Override
    public @NotNull String getCommandName()
    {
        return "unmorph";
    }

    @Override
    public @Nullable String getPermissionRequirement()
    {
        return "xiamomc.morph.manage.unmorph";
    }

    @Resolved
    private MorphManager manager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, String[] args)
    {
        if (args.length < 1)
        {
            sender.sendMessage(MessageUtils.prefixes(sender, Component.text("未指定取消谁的伪装").color(NamedTextColor.RED)));

            return true;
        }

        if (Objects.equals(args[0], "*"))
        {
            manager.unMorphAll(true);
            sender.sendMessage(MessageUtils.prefixes(sender, Component.text("成功取消所有人的伪装！")));

            return true;
        }

        var player = Bukkit.getPlayer(args[0]);
        if (player == null)
        {
            sender.sendMessage(MessageUtils.prefixes(sender, Component.text("未找到此玩家").color(NamedTextColor.RED)));

            return true;
        }

        manager.unMorph(player);

        sender.sendMessage(MessageUtils.prefixes(sender, Component.text("成功取消" + player.getName() + "的伪装！")));

        return true;
    }

    @Override
    public @Nullable String getHelpMessage()
    {
        return "取消某人的伪装";
    }
}
