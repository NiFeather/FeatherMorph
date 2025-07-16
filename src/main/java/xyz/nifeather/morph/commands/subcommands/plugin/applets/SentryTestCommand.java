package xyz.nifeather.morph.commands.subcommands.plugin.applets;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.commands.brigadier.BrigadierCommand;

public class SentryTestCommand extends BrigadierCommand
{
    @Override
    public @Nullable String getPermissionRequirement()
    {
        return null;
    }

    @Override
    public void registerAsChild(ArgumentBuilder<CommandSourceStack, ?> parentBuilder)
    {
        parentBuilder.then(
                Commands.literal(name())
                        .executes(this::execute)
        );
        super.registerAsChild(parentBuilder);
    }

    private int execute(CommandContext<CommandSourceStack> context)
    {
        var sender = context.getSource().getSender();
        if (!(sender instanceof Player player))
            return 0;

        sender.sendMessage("Preparing to trigger error!");
        featherMorph().getPlatform().runAsyncDelayed(() ->
        {
            player.getNearbyEntities(10, 10, 10);
        }, 20);

        return 1;
    }

    @Override
    public @NotNull String name()
    {
        return "test_sentry";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return null;
    }
}
