package xyz.nifeather.morph.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.commands.brigadier.BrigadierCommand;
import xyz.nifeather.morph.commands.brigadier.arguments.ValueMapArgumentType;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.messages.strings.MorphStrings;
import xyz.nifeather.morph.misc.BoundingBoxLookup;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.ParseErrorException;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyValidationException;
import xyz.nifeather.morph.misc.disguiseProperty.ValidationFlag;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.skills.MorphSkill;
import xyz.nifeather.morph.utilities.ExceptionUtils;

import java.util.EnumSet;

public class ModifyDisguiseCommand extends BrigadierCommand
{
    @Override
    public @Nullable String getPermissionRequirement()
    {
        return CommonPermissions.MORPH;
    }

    @Override
    public @NotNull String name()
    {
        return "modify-morph";
    }

    @Override
    public FormattableMessage getHelpMessage()
    {
        return null;
    }

    private final ValueMapArgumentType propertyArgument = new ValueMapArgumentType();

    @Initializer
    private void load()
    {
        DisguiseProperties.INSTANCE.getAllProperties().forEach(property ->
        {
            if (property.hideFromUserInput())
                return;

            var name = property.id();
            var values = property.validInputs();

            propertyArgument.setProperty(name, values);
        });
    }

    @Override
    public boolean register(Commands dispatcher)
    {
        dispatcher.register(
                Commands.literal(name())
                        .requires(this::checkPermission)
                        .then(
                                Commands.argument("properties", propertyArgument)
                                        .executes(this::mergeProperties)
                        )
                        .build()
        );

        return true;
    }

    @Resolved
    private MorphManager morphManager;

    private int mergeProperties(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException
    {
        var state = morphManager.getDisguiseStateFor(context.getSource().getExecutor());
        if (state == null)
        {
            MessageUtils.send(context.getSource().getSender(), CommandStrings.notDisguised());
            return 0;
        }

        var propertiesInput = ValueMapArgumentType.get("properties", context);
        try
        {
            state.disguisePropertyHandler().updateFromPropertiesInput(propertiesInput, context.getSource().getExecutor(), EnumSet.noneOf(ValidationFlag.class));
        }
        catch (ParseErrorException | PropertyValidationException e)
        {
            FormattableMessage message = switch (e)
            {
                case PropertyValidationException propertyValidationException ->
                        MorphStrings.errorValidatingProperty().resolve("what", propertyValidationException.propertyName);

                case ParseErrorException parseErrorException ->
                        MorphStrings.errorParsingProperty().resolve("what", parseErrorException.propertyName);

                default -> MorphStrings.errorWhileDisguisingWithError();
            };

            message.resolve("error", ExceptionUtils.getExceptionMessageShort(e));

            MessageUtils.send(context.getSource().getSender(), message, c -> c.hoverEvent(HoverEvent.showText(ExceptionUtils.getExceptionDetail(e))));
            return 0;
        }

        MessageUtils.send(context.getSource().getSender(), CommandStrings.success());

        if (context.getSource().getExecutor() instanceof Player player)
        {
            double cX, cY, cZ;

            var box = BoundingBoxLookup.instance().getBoundboxOptional(state.getEntityType(), player.getLocation())
                    .orElse(BoundingBox.of(player.getLocation().getBlock()));

            cX = cZ = box.getWidthX();
            cY = box.getHeight();

            morphManager.spawnCloudParticle(player, player.getLocation(), cX, cY, cZ);
        }

        return 1;
    }
}
