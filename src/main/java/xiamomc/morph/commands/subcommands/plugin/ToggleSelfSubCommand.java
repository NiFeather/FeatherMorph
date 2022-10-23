package xiamomc.morph.commands.subcommands.plugin;

import me.libraryaddict.disguise.DisguiseAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.interfaces.IManagePlayerData;
import xiamomc.morph.messages.HelpStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphStrings;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.messages.FormattableMessage;

public class ToggleSelfSubCommand extends MorphPluginObject implements ISubCommand
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

    @Resolved
    private IManagePlayerData data;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (sender instanceof Player player)
        {
            var config = data.getPlayerConfiguration(player);

            var targetVisibility = !config.showDisguiseToSelf;
            DisguiseAPI.setViewDisguiseToggled(player, targetVisibility);
            config.showDisguiseToSelf = targetVisibility;

            sender.sendMessage(MessageUtils.prefixes(sender, targetVisibility
                    ? MorphStrings.selfVisibleOnString()
                    : MorphStrings.selfVisibleOffString()));
        }
        return true;
    }
}
