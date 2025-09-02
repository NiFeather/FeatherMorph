package xyz.nifeather.morph.misc.disguiseProperty.values;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Entity;
import xyz.nifeather.morph.messages.ExceptionStrings;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Map;
import java.util.Optional;

public abstract class BaseLivingEntityProperties<E extends Entity> extends AbstractProperties<E>
{
    protected SingleProperty<Component> createCustomNameProperty()
    {
        return getSingle(PropertyNames.ENTITY_CUSTOM_NAME, Component.empty(), this::readCustomName);
    }

    public final SingleProperty<Component> CUSTOM_NAME = createCustomNameProperty();

    private Optional<Component> readCustomName(String propertyName, String string) throws ParseErrorException
    {
        if (string.length() > 256)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readCustomName")
                    .withMessage("Given input is too long!")
                    .withLocalizableMessage(ExceptionStrings.inputTooLong())
                    .create();
        }

        if (string.isBlank())
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readCustomName")
                    .withMessage("Blank string for custom name")
                    .withLocalizableMessage(ExceptionStrings.noEmptyInput())
                    .create();
        }

        var component = InputHandles.readAdventureComponent(propertyName, string);

        if (component.isPresent())
        {
            var finalText = PlainTextComponentSerializer.plainText().serialize(component.get());

            if (finalText.isBlank())
            {
                throw ParseErrorException.forProperty(propertyName)
                        .byMethod("readCustomName")
                        .withMessage("Blank component is not allowed")
                        .withLocalizableMessage(ExceptionStrings.noEmptyInput())
                        .create();
            }
        }

        return component;
    }

    public final SingleProperty<Boolean> CUSTOM_NAME_VISIBLE = getSingle(PropertyNames.ENTITY_CUSTOM_NAME_VISIBLE, false, InputHandles::readBooleanRelaxed)
            .withValidInput("true", "false");

    public final SingleProperty<Integer> STUCKED_ARROWS = getSingle(PropertyNames.ENTITY_ARROW_COUNT, 0, this::readArrows);

    private Optional<Integer> readArrows(String propertyName, String string) throws ParseErrorException
    {
        // localizable message not required, since readInteger always return a value or throw ParseErrorException
        var val = InputHandles.readInteger(propertyName, string)
                .orElseThrow(() -> new ParseErrorException(propertyName, "readArrows: Unable to parse arrows"));

        InputHandles.throwIfOutOfBounds(propertyName, val, 0, 100);

        return Optional.of(val);
    }

    public BaseLivingEntityProperties()
    {
        registerSingle(CUSTOM_NAME, CUSTOM_NAME_VISIBLE, STUCKED_ARROWS);
    }

    private static final Component minimessageFormatFail = Component.text("MiniMessage format error");
    private static final Component minimessageCastFail = Component.text("MiniMessage cast error");

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        propertyHandler.getOptional(CUSTOM_NAME).ifPresent(component ->
                map.put(CUSTOM_NAME.id(), JSONComponentSerializer.json().serialize(component)));

        propertyHandler.getOptional(CUSTOM_NAME_VISIBLE).ifPresent(v -> map.put(CUSTOM_NAME_VISIBLE.id(), v.toString().toLowerCase()));

        propertyHandler.getOptional(STUCKED_ARROWS).ifPresent(v -> map.put(STUCKED_ARROWS.id(), v.toString()));
    }
}
