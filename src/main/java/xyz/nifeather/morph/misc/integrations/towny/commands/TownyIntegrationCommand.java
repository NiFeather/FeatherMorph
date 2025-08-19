package xyz.nifeather.morph.misc.integrations.towny.commands;

import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import io.papermc.paper.command.brigadier.Commands;
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
                //getBooleanToggle(TownyFlags.ALLOW_FLIGHT_IN_TOWN_MASTERTOGGLE),
                getBooleanToggle(TownyFlags.ALLOW_OUTSIDERS_FLIGHT),
                getBooleanToggle(TownyFlags.ALLOW_OUTSIDERS_USE_SKILL)
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
                .requires(this::checkPermission);

        subCommands.forEach(cmd -> cmd.registerAsChild(command));

        dispatcher.register(command.build());

        return true;
    }

    @Override
    public @NotNull String name()
    {
        return "town_morph_flags";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return null;
    }
}
