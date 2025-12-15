package xyz.nifeather.morph.commands.subcommands.plugin;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.commands.brigadier.BrigadierCommand;
import xyz.nifeather.morph.storage.IPlayerDataBackend;
import xyz.nifeather.morph.messages.strings.HelpStrings;

public class ToggleSelfSubCommand extends BrigadierCommand
{
    @Override
    public String name()
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

    @Override
    public void registerAsChild(ArgumentBuilder<CommandSourceStack, ?> parentBuilder)
    {
        parentBuilder.then(
                Commands.literal(name())
                        .requires(this::checkPermission)
                        .executes(this::executes)
        );

        super.registerAsChild(parentBuilder);
    }

    public int executes(CommandContext<CommandSourceStack> context)
    {
        if (!(context.getSource().getExecutor() instanceof Player player))
            return 1;

        manager.getDataStore().getOrLoad(player.getUniqueId())
                        .thenAccept(meta -> manager.setSelfDisguiseVisible(player, !meta.showDisguiseToSelf, true));

        return 1;
    }
}
