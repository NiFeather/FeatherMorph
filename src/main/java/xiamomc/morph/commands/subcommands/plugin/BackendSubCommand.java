package xiamomc.morph.commands.subcommands.plugin;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.DisguiseBackend;
import xiamomc.morph.messages.BackendStrings;
import xiamomc.morph.messages.CommandStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.misc.permissions.CommonPermissions;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Command.ISubCommand;
import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.List;

public class BackendSubCommand extends MorphPluginObject implements ISubCommand
{
    @Resolved(shouldSolveImmediately = true)
    private MorphManager manager;

    @Override
    public @NotNull String getCommandName()
    {
        return "switch_backend";
    }

    @Override
    public @Nullable List<String> onTabComplete(List<String> args, CommandSender source)
    {
        var targetName = args.isEmpty() ? "" : args.get(0);
        targetName = targetName.toLowerCase();

        String finalTargetName = targetName;
        return manager.listManagedBackends()
                .stream()
                .map(DisguiseBackend::getIdentifier)
                .filter(id -> id.contains(finalTargetName))
                .toList();
    }

    @Override
    public @Nullable String getPermissionRequirement()
    {
        return CommonPermissions.SET_BACKEND;
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return new FormattableMessage(MorphPlugin.getInstance(), "Switch backend");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String[] args)
    {
        if (args.length < 1)
        {
            sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.listNoEnoughArguments()));
            return true;
        }

        var targetName = args[0];
        var backend = manager.getBackend(targetName);

        if (backend == null)
        {
            sender.sendMessage(MessageUtils.prefixes(sender, BackendStrings.noSuchBackend()));
            return true;
        }

        if (!manager.switchBackend(backend))
            sender.sendMessage(MessageUtils.prefixes(sender, BackendStrings.switchFailed()));

        sender.sendMessage(
                MessageUtils.prefixes(
                        sender,
                        BackendStrings.switchSuccess()
                                .resolve("name",
                                        backend.getDisplayName()
                                                .withLocale(MessageUtils.getLocale(sender))
                                )
                )
        );

        sender.sendMessage(MessageUtils.prefixes(sender, BackendStrings.experimentalWarning()));

        return true;
    }
}
