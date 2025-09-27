package xyz.nifeather.morph.misc.disguiseProperty.values;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.messages.ExceptionStrings;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Optional;

public abstract class BaseLivingEntityProperties<E extends Entity> extends AbstractProperties<E>
{
    protected SingleProperty<Component> createCustomNameProperty()
    {
        return createProperty(PropertyNames.ENTITY_CUSTOM_NAME, Component.empty(), this::readCustomNameMiniMessage, OutputHandles::writeAdventureComponentJSON);
    }

    public final SingleProperty<Boolean> CUSTOM_NAME_VISIBLE = createProperty(PropertyNames.ENTITY_CUSTOM_NAME_VISIBLE, false, InputHandles::readBooleanRelaxed, OutputHandles::writeBoolean)
            .withValidInput("true", "false");

    public final SingleProperty<Integer> STUCKED_ARROWS = createProperty(PropertyNames.ENTITY_ARROW_COUNT, 0, this::readArrows, OutputHandles::writeInteger);

    public final SingleProperty<Component> CUSTOM_NAME = createCustomNameProperty();

    public final SingleProperty<DisguiseEquipment> EQUIPMENT = SingleProperty.of(PropertyNames.ENTITY_EQUIPMENT, new DisguiseEquipment(), InputHandles::unsupported, OutputHandles::writeEquipment, true);
    public final SingleProperty<Boolean> DISPLAY_DISGUISE_EQUIPMENT = createProperty(PropertyNames.ENTITY_DISPLAY_DISGUISE_EQUIPMENT, false, InputHandles::readBooleanRelaxed, OutputHandles::writeBoolean);

    private Optional<Component> readCustomNameMiniMessage(String propertyName, String string) throws ParseErrorException
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

            if (finalText.length() > 50)
            {
                throw ParseErrorException.forProperty(propertyName)
                        .byMethod("readCustomName")
                        .withMessage("The final name input is too long!")
                        .withLocalizableMessage(ExceptionStrings.inputTooLong())
                        .create();
            }

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

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull E targetEntity)
    {
        propertyHandler.set(CUSTOM_NAME_VISIBLE, targetEntity.isCustomNameVisible());

        var entityCustomName = targetEntity.customName();

        if (entityCustomName != null)
            propertyHandler.set(CUSTOM_NAME, entityCustomName);

        // equipment properties setup is currently hand-off to InventoryMorphSkill#onInitialEquip
    }

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
        registerSingle(CUSTOM_NAME, CUSTOM_NAME_VISIBLE, STUCKED_ARROWS, EQUIPMENT, DISPLAY_DISGUISE_EQUIPMENT);
    }

    private static final Component minimessageFormatFail = Component.text("MiniMessage format error");
    private static final Component minimessageCastFail = Component.text("MiniMessage cast error");

}
