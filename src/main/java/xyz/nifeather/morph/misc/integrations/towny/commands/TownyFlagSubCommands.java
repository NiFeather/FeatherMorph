package xyz.nifeather.morph.misc.integrations.towny.commands;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.commands.brigadier.BrigadierCommand;
import xyz.nifeather.morph.events.api.gameplay.MorphTownBooleanFlagChangedEvent;
import xyz.nifeather.morph.messages.CommandStrings;
import xyz.nifeather.morph.messages.MessageUtils;

public class TownyFlagSubCommands
{
    public static class BooleanFlagCommand extends BrigadierCommand
    {
        private final String name;
        private final BooleanDataField dataField;

        public BooleanFlagCommand(BooleanDataField dataField)
        {
            this.dataField = dataField;
            this.name = dataField.getKey().replaceFirst("feathermorph_", "");
        }

        @Override
        public void registerAsChild(ArgumentBuilder<CommandSourceStack, ?> parentBuilder)
        {
            parentBuilder.then(
                    Commands.literal(name())
                            .executes(this::execLookup)
                            .then(
                                    Commands.argument("new_value", BoolArgumentType.bool())
                                            .executes(this::execSetValue)
                            )
            );
        }

        private int execSetValue(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
        {
            if (!(context.getSource().getExecutor() instanceof Player player))
                throw new SimpleCommandExceptionType((Message) Component.text("Panic! The executor is not a player!")).create();

            var towny = TownyAPI.getInstance();
            var town = towny.getTown(player);
            var resident = towny.getResident(player);
            var sender = context.getSource().getSender();

            if (town == null)
            {
                sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.townyDoesntHaveTown()));
                return 0;
            }

            if (resident == null)
            {
                sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.unknownError()));
                return 0;
            }

            if (!town.isMayor(resident))
            {
                sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.townyPlayerNotMayor()));
                return 0;
            }

            var value = BoolArgumentType.getBool(context, "new_value");

            MetaDataUtil.setBoolean(town, dataField, value, true);
            new MorphTownBooleanFlagChangedEvent(player, town, dataField, value).callEvent();

            sender.sendMessage("Set '%s' of '%s' to '%s'".formatted(name(), town.getName(), value));

            return 1;
        }

        private int execLookup(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
        {
            if (!(context.getSource().getExecutor() instanceof Player player))
                throw new SimpleCommandExceptionType((Message) Component.text("Panic! The executor is not a player!")).create();

            var towny = TownyAPI.getInstance();
            var town = towny.getTown(player);
            var resident = towny.getResident(player);
            var sender = context.getSource().getSender();

            if (town == null)
            {
                sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.townyDoesntHaveTown()));
                return 0;
            }

            if (resident == null)
            {
                sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.unknownError()));
                return 0;
            }

            if (!town.isMayor(resident))
            {
                sender.sendMessage(MessageUtils.prefixes(sender, CommandStrings.townyPlayerNotMayor()));
                return 0;
            }

            if (!MetaDataUtil.hasMeta(town, dataField))
            {
                sender.sendMessage("The town does not have flag '%s' set yet! And will use the defalut value '%s'".formatted(name(), dataField.getValue()));
                return 1;
            }

            if (MetaDataUtil.getBoolean(town, dataField))
                sender.sendMessage("The town has flag '%s' turned on!".formatted(name()));
            else
                sender.sendMessage("The town has flag '%s' turned off!".formatted(name()));

            return 1;
        }

        @Override
        public @Nullable String getPermissionRequirement()
        {
            return null;
        }

        @Override
        public @NotNull String name()
        {
            return name;
        }

        @Override
        public FormattableMessage getHelpMessage()
        {
            return null;
        }
    }
}
