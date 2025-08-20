package xyz.nifeather.morph.misc.disguiseProperty.values;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.entity.Entity;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Map;
import java.util.Optional;

public abstract class BaseLivingEntityProperties<E extends Entity> extends AbstractProperties<E>
{
    public final SingleProperty<Component> CUSTOM_NAME = getSingle(PropertyNames.ENTITY_CUSTOM_NAME, Component.empty(), this::readCustomName);

    private Optional<Component> readCustomName(String string) throws ParseErrorException
    {
        if (string.length() > 256)
            throw new ParseErrorException("readCustomName: Input string is too long!");

        return InputHandles.readAdventureComponent(string);
    }

    public final SingleProperty<Boolean> CUSTOM_NAME_VISIBLE = getSingle(PropertyNames.ENTITY_CUSTOM_NAME_VISIBLE, false, InputHandles::readBooleanRelaxed)
            .withValidInput("true", "false");

    public final SingleProperty<Integer> STUCKED_ARROWS = getSingle(PropertyNames.ENTITY_ARROW_COUNT, 0, this::readArrows);

    private Optional<Integer> readArrows(String string) throws ParseErrorException
    {
        var val = InputHandles.readInteger(string)
                .orElseThrow(() -> new ParseErrorException("readArrows: Unable to parse arrows"));

        InputHandles.throwIfOutOfBounds(val, 0, 100);

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
