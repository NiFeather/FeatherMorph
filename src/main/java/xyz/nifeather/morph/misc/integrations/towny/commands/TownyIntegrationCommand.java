package xyz.nifeather.morph.misc.integrations.towny.commands;

import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.commands.brigadier.BrigadierCommand;
import xyz.nifeather.morph.commands.brigadier.IConvertibleBrigadier;
import xyz.nifeather.morph.misc.integrations.towny.TownyFlags;

import java.util.List;

public class TownyIntegrationCommand extends BrigadierCommand
{
    public TownyIntegrationCommand()
    {
        subCommands = List.of(
                getBooleanToggle(TownyFlags.ALLOW_OUTSIDERS_FLY_IN_TOWN)
                //todo: Toggle whether outsiders can use skills
                //todo: Toggle whether outsiders can fly?
        );
    }

    private IConvertibleBrigadier getBooleanToggle(BooleanDataField dataField)
    {
        return new TownyFlagSubCommands.BooleanFlagCommand(dataField);
    }

    private final List<IConvertibleBrigadier> subCommands;

    @Override
    public @Nullable String getPermissionRequirement()
    {
        return null;
    }

    @Override
    public boolean register(Commands dispatcher)
    {
        var command = Commands.literal(name())
                .requires(context ->
                {
                    if (!(context.getSender() instanceof Player))
                    {
                        context.getSender().sendMessage("This command is currently only available to players.");
                        return false;
                    }

                    return this.checkPermission(context);
                });

        subCommands.forEach(cmd -> cmd.registerAsChild(command));

        dispatcher.register(command.build());

        return true;
    }

    @Override
    public @NotNull String name()
    {
        return "morph_town_flags";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return null;
    }
}
