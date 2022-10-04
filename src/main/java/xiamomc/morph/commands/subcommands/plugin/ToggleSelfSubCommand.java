package xiamomc.morph.commands.subcommands.plugin;

import me.libraryaddict.disguise.DisguiseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.misc.MessageUtils;
import xiamomc.pluginbase.Command.ISubCommand;

public class ToggleSelfSubCommand implements ISubCommand
{

    @Override
    public String getCommandName()
    {
        return "toggleself";
    }

    @Override
    public String getPermissionRequirement()
    {
        return null;
    }

    @Override
    public String getHelpMessage()
    {
        return "切换自身可见性";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (sender instanceof Player player)
        {
            var targetVisibility = !DisguiseAPI.isViewSelfToggled(player);
            DisguiseAPI.setViewDisguiseToggled(player, targetVisibility);

            sender.sendMessage(MessageUtils.prefixes(sender, Component.text("已切换自身可见性")
                    .color(targetVisibility ? NamedTextColor.GREEN : NamedTextColor.RED)));
        }
        return true;
    }
}
