package xyz.nifeather.morph.commands.subcommands.plugin;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.commands.brigadier.BrigadierCommand;
import xyz.nifeather.morph.commands.brigadier.IConvertibleBrigadier;
import xyz.nifeather.morph.commands.subcommands.plugin.applets.SchedulerTestCommand;
import xyz.nifeather.morph.commands.subcommands.plugin.applets.SentryTestCommand;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

import java.util.List;

public class ToolsSubCommand extends BrigadierCommand
{
    @Override
    public @Nullable String getPermissionRequirement()
    {
        return CommonPermissions.ADMIN;
    }

    public ToolsSubCommand()
    {
        subCommands = List.of(
                new SchedulerTestCommand(),
                new SentryTestCommand()
        );
    }

    private final List<IConvertibleBrigadier> subCommands;

    @Override
    public void registerAsChild(ArgumentBuilder<CommandSourceStack, ?> parentBuilder)
    {
        var cmd = Commands.literal(name());

        subCommands.forEach(c -> c.registerAsChild(cmd));

        parentBuilder.then(cmd);

        super.registerAsChild(parentBuilder);
    }

    @Override
    public @NotNull String name()
    {
        return "tools";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return null;
    }
}
