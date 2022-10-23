package xiamomc.morph.commands.subcommands.plugin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.interfaces.IManagePlayerData;
import xiamomc.morph.messages.HelpStrings;
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
    private MorphManager manager;

    @Resolved
    private IManagePlayerData data;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (sender instanceof Player player)
        {
            manager.setSelfDisguiseVisible(player, !data.getPlayerConfiguration(player).showDisguiseToSelf, true);
        }
        return true;
    }
}
