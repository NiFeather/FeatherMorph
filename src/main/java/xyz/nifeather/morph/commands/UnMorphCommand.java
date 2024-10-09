package xyz.nifeather.morph.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.messages.HelpStrings;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.IPluginCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;

public class UnMorphCommand extends MorphPluginObject implements IPluginCommand
{
    @Override
    public String getCommandName()
    {
        return "unmorph";
    }

    @Override
    public String getPermissionRequirement()
    {
        return CommonPermissions.UNMORPH;
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.unMorphDescription();
    }

    @Resolved
    private MorphManager morphs;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        if (sender instanceof Player player)
            morphs.unMorph(player);

        return true;
    }
}
