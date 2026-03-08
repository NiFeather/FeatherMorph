package xyz.nifeather.morph.commands.subcommands.plugin.extract;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.commands.brigadier.BrigadierCommand;
import xyz.nifeather.morph.commands.brigadier.IConvertibleBrigadier;
import xyz.nifeather.morph.messages.strings.HelpStrings;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

public class ExtractSubCommand extends BrigadierCommand
{
    @Override
    public @Nullable String getPermissionRequirement()
    {
        return CommonPermissions.ADMIN;
    }

    private final IConvertibleBrigadier[] subCmd = new IConvertibleBrigadier[]
            {
                    new DumpLanguageCommand()
            };

    @Override
    public void registerAsChild(ArgumentBuilder<CommandSourceStack, ?> parentBuilder)
    {
        var cmd = Commands.literal(name()).requires(this::checkPermission);

        for (IConvertibleBrigadier icb : subCmd)
            icb.registerAsChild(cmd);

        parentBuilder.then(cmd.build());

        super.registerAsChild(parentBuilder);
    }

    @Override
    public @NotNull String name()
    {
        return "extract";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.extractDescription();
    }
}
