package xiamomc.morph.commands.subcommands.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.commands.subcommands.ISubCommand;
import xiamomc.morph.misc.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.List;

public class ReloadSubCommand extends MorphPluginObject implements ISubCommand {
    @Override
    public List<String> onTabComplete(String[] args, CommandSender source) {
        return null;
    }

    @Override
    public String getSubCommandName() {
        return "reload";
    }

    @Override
    public String getPermissionRequirement() {
        return "xiamomc.morph.reload";
    }

    @Resolved
    private MorphManager morphManager;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (sender.hasPermission(getPermissionRequirement()))
        {
            morphManager.reloadConfiguration();
            sender.sendMessage(MessageUtils.prefixes(Component.text("重载完成！")));
        }
        else
            sender.sendMessage(MessageUtils.prefixes(Component.text("禁止接触").color(NamedTextColor.RED)));

        return true;
    }
}
