package xiamomc.morph.commands.subcommands.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;

import java.util.List;

public class ReloadSubCommand extends MorphPluginObject implements ISubCommand {
    @Override
    public List<String> onTabComplete(List<String> args, CommandSender source) {
        return null;
    }

    @Override
    public String getCommandName() {
        return "reload";
    }

    @Override
    public String getPermissionRequirement() {
        return "xiamomc.morph.reload";
    }

    @Override
    public String getHelpMessage() {
        return "重载插件配置";
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
