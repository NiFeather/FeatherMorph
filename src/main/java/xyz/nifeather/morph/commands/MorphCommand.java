package xyz.nifeather.morph.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.commands.brigadier.IConvertibleBrigadier;
import xyz.nifeather.morph.commands.brigadier.arguments.DisguiseIdentifierArgumentType;
import xyz.nifeather.morph.commands.brigadier.arguments.ValueMapArgumentType;
import xyz.nifeather.morph.messages.strings.HelpStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.MorphStrings;
import xyz.nifeather.morph.misc.MorphParameters;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.gui.DisguiseSelectScreenWrapper;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class MorphCommand extends MorphPluginObject implements IConvertibleBrigadier
{
    @Resolved
    private MorphManager morphManager;

    @Override
    public @NotNull String name()
    {
        return "morph";
    }

    private final ValueMapArgumentType propertyArgument = new ValueMapArgumentType();

    @Override
    public boolean register(Commands dispatcher)
    {
        dispatcher.register(Commands.literal(name())
                .requires(this::checkPermission)
                .executes(this::executeNoArg)
                        .then(
                                Commands.argument("id", DisguiseIdentifierArgumentType.FOR_PLAYER)
                                        .executes(this::execWithID)
                                        .then(
                                                Commands.argument("properties", propertyArgument)
                                                        .executes(this::execWithProperties)
                                        )
                        )
                .build());

        return true;
    }

    @Initializer
    private void load()
    {
        DisguiseProperties.INSTANCE.getAll().forEach((type, properties) ->
        {
            for (SingleProperty<?> property : properties.getRegisteredProperties().values())
            {
                if (property.hideFromUserInput())
                    continue;

                var name = property.id();
                var values = property.validInputs();

                propertyArgument.setProperty(name, values);
            }
        });
    }

    public int executeNoArg(CommandContext<CommandSourceStack> context)
    {
        var sender = context.getSource().getExecutor();

        if (!(sender instanceof Player player))
            return Command.SINGLE_SUCCESS;

        //伪装冷却
        if (!morphManager.canMorph(player))
        {
            MessageUtils.send(player, MorphStrings.disguiseCoolingDownString());

            return Command.SINGLE_SUCCESS;
        }

        var gui = new DisguiseSelectScreenWrapper(player);
        gui.show();

        return 1;
    }

    private int execWithID(CommandContext<CommandSourceStack> context)
    {
        var sender = context.getSource().getSender();
        var executor = context.getSource().getExecutor();

        if (!(executor instanceof Player player))
        {
            MessageUtils.send(sender, "Only players can execute disguise command");

            return Command.SINGLE_SUCCESS;
        }

        String inputID = DisguiseIdentifierArgumentType.getArgument(context, "id");
        this.doDisguise(context.getSource().getSender(), player, inputID, null);

        return 1;
    }

    private void doDisguise(CommandSender sender, Player who, String id, @Nullable Map<String, String> properties)
    {
        //伪装冷却
        if (!morphManager.canMorph(who))
        {
            MessageUtils.send(sender, MorphStrings.disguiseCoolingDownString());
            return;
        }

        var parameters = MorphParameters.create(who, id)
                .setSource(sender)
                .setTargetedEntity(who.getTargetEntity(5));

        if (properties != null)
            parameters.withProperties(properties);

        morphManager.morph(parameters);
    }

    private int execWithProperties(CommandContext<CommandSourceStack> context)
    {
        var sender = context.getSource().getSender();
        var executor = context.getSource().getExecutor();

        if (!(executor instanceof Player player))
        {
            MessageUtils.send(sender, "Only players can execute disguise command");

            return Command.SINGLE_SUCCESS;
        }

        var propertiesInput = ValueMapArgumentType.get("properties", context);
        var disguiseInput =  DisguiseIdentifierArgumentType.getArgument(context, "id");

        this.doDisguise(sender, player, disguiseInput, propertiesInput);

        return 1;
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return HelpStrings.morphDescription();
    }
}
