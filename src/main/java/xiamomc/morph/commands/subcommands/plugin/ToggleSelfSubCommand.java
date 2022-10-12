package xiamomc.morph.commands.subcommands.plugin;

import me.libraryaddict.disguise.DisguiseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.messages.HelpStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.messages.FormattableMessage;

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
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.toggleSelfDescription();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (sender instanceof Player player)
        {
            var targetVisibility = !DisguiseAPI.isViewSelfToggled(player);
            DisguiseAPI.setViewDisguiseToggled(player, targetVisibility);

            sender.sendMessage(MessageUtils.prefixes(sender, targetVisibility
                    ? MorphStrings.selfVisibleOnString()
                    : MorphStrings.selfVisibleOffString()));
        }
        return true;
    }
}
