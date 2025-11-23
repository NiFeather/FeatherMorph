package xyz.nifeather.morph.misc.disguiseProperty.values;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Optional;

public abstract class BaseLivingEntityProperties<E extends Entity> extends AbstractProperties<E>
{
    protected SingleProperty<Component> createCustomNameProperty()
    {
        return SingleProperty.builder(PropertyNames.ENTITY_CUSTOM_NAME, Component.class, Component.empty())
                .withInputHandle(InputHandles::readComponentAny)
                .withOutputHandle(OutputHandles::writeAdventureComponentJSON)
                .withValidator(PropertyValidations::validateCustomTextPermission)
                .build();
    }

    public final SingleProperty<Boolean> CUSTOM_NAME_VISIBLE = createProperty(PropertyNames.ENTITY_CUSTOM_NAME_VISIBLE, false, InputHandles::readBooleanRelaxed, OutputHandles::writeBoolean)
            .withValidInput("true", "false");

    public final SingleProperty<Integer> STUCKED_ARROWS = createProperty(PropertyNames.ENTITY_ARROW_COUNT, 0, this::readArrows, OutputHandles::writeInteger);

    public final SingleProperty<Component> CUSTOM_NAME = createCustomNameProperty();

    public final SingleProperty<DisguiseEquipment> EQUIPMENT = SingleProperty.builder(PropertyNames.ENTITY_EQUIPMENT, DisguiseEquipment.class, DisguiseEquipment.empty())
            .withInputHandle(InputHandles::readEquipment)
            .withOutputHandle(OutputHandles::writeEquipment)
            .withValidator(PropertyValidations::validateEquipment)
            .withHideFromUserInput(true)
            .build();

    public final SingleProperty<Boolean> DISPLAY_DISGUISE_EQUIPMENT = SingleProperty.of(PropertyNames.ENTITY_DISPLAY_DISGUISE_EQUIPMENT, false, InputHandles::readBooleanStrict, OutputHandles::writeBoolean, true);

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull E targetEntity)
    {
        if (!meta.getEntityType().equals(targetEntity.getType()))
            return;

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
}
