package xiamomc.morph.commands.subcommands.plugin;

import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.events.api.lifecycle.ConfigurationReloadEvent;
import xiamomc.morph.messages.*;
import xiamomc.morph.messages.vanilla.VanillaMessageStore;
import xiamomc.morph.network.server.MorphClientHandler;
import xiamomc.morph.storage.skill.SkillAbilityConfigurationStore;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xiamomc.pluginbase.Messages.MessageStore;

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
    private SkillAbilityConfigurationStore skills;

    private final List<String> subcommands = ObjectImmutableList.of("data", "message", "update_message");

    @Override
    public List<String> onTabComplete(List<String> args, CommandSender source)
    {
        if (source.hasPermission(getPermissionRequirement()) && args.size() >= 1)
            return subcommands.stream().filter(s -> s.startsWith(args.get(0))).toList();
        else
            return null;
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
            var reloadOverwriteNonDefMsg = false;
            String option = args.length >= 1 ? args[0] : "*";

            switch (option)
            {
                case "data" -> reloadsData = true;
                case "message" -> reloadsMessage = true;
                case "update_message" -> reloadsMessage = reloadOverwriteNonDefMsg = true;
                default -> reloadsMessage = reloadsData = true;
            }

            if (reloadsData)
            {
                config.reload();
                skills.reloadConfiguration();
                morphManager.reloadConfiguration();
            }

            if (reloadsMessage)
            {
                if (reloadOverwriteNonDefMsg && messageStore instanceof MorphMessageStore morphMessageStore)
                    morphMessageStore.reloadOverwriteNonDefault();
                else
                    messageStore.reloadConfiguration();

                vanillaMessageStore.reloadConfiguration();
            }

            var event = new ConfigurationReloadEvent(reloadsData, reloadsMessage);
            event.callEvent();

            sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.reloadCompleteMessage()));
        }
        else
            sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.noPermissionMessage()));

        return true;
    }
}
