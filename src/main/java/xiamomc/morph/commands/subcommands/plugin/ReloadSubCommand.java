package xiamomc.morph.commands.subcommands.plugin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.messages.CommandStrings;
import xiamomc.morph.messages.HelpStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.vanilla.VanillaMessageStore;
import xiamomc.morph.network.MorphClientHandler;
import xiamomc.morph.storage.skill.SkillConfigurationStore;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xiamomc.pluginbase.Messages.MessageStore;

import java.util.Arrays;
import java.util.List;

public class ReloadSubCommand extends MorphPluginObject implements ISubCommand
{
    @Override
    public String getCommandName()
    {
        return "reload";
    }

    @Override
    @NotNull
    public String getPermissionRequirement()
    {
        return "xiamomc.morph.reload";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.reloadDescription();
    }

    @Resolved
    private MorphManager morphManager;

    @Resolved
    private MorphConfigManager config;

    @Resolved
    private MessageStore<?> messageStore;

    @Resolved
    private VanillaMessageStore vanillaMessageStore;

    @Resolved
    private SkillConfigurationStore skills;

    private final String[] subcommands = new String[]
            {
              "data",
              "message"
            };

    @Override
    public List<String> onTabComplete(List<String> args, CommandSender source)
    {
        if (source.hasPermission(getPermissionRequirement()) && args.size() >= 1)
        {
            return Arrays.stream(subcommands).filter(s -> s.startsWith(args.get(0))).toList();
        }
        else return null;
    }

    @Resolved
    private MorphClientHandler clientHandler;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (sender.hasPermission(getPermissionRequirement()))
        {
            var reloadsData = false;
            var reloadsMessage = false;
            String option = args.length >= 1 ? args[0] : "*";

            switch (option)
            {
                case "data" -> reloadsData = true;
                case "message" -> reloadsMessage = true;
                default -> reloadsMessage = reloadsData = true;
            }

            if (reloadsData)
            {
                config.reload();
                skills.reloadConfiguration();
                morphManager.reloadConfiguration();

                if (config.get(Boolean.class, ConfigOption.FORCE_TARGET_VERSION))
                {
                    clientHandler.sendUnAuth(Bukkit.getOnlinePlayers());
                    clientHandler.sendReAuth(Bukkit.getOnlinePlayers());
                }
            }

            if (reloadsMessage)
            {
                messageStore.reloadConfiguration();
                vanillaMessageStore.reloadConfiguration();
            }

            sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.reloadCompleteMessage()));
        }
        else
            sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.noPermissionMessage()));

        return true;
    }
}
