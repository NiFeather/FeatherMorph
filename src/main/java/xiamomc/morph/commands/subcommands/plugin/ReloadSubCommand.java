package xiamomc.morph.commands.subcommands.plugin;

import com.google.protobuf.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.messages.CommandStrings;
import xiamomc.morph.messages.MessageStore;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;

public class ReloadSubCommand extends MorphPluginObject implements ISubCommand
{

    @Override
    public String getCommandName()
    {
        return "reload";
    }

    @Override
    public String getPermissionRequirement()
    {
        return "xiamomc.morph.reload";
    }

    @Override
    public String getHelpMessage()
    {
        return "重载插件配置";
    }

    @Resolved
    private MorphManager morphManager;

    @Resolved
    private MorphConfigManager config;

    @Resolved
    private MessageStore messageStore;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (sender.hasPermission(getPermissionRequirement()))
        {
            morphManager.reloadConfiguration();
            config.reload();
            messageStore.reloadConfiguration();

            sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.reloadCompleteMessage()));
        }
        else
            sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.noPermissionMessage()));

        return true;
    }
}
