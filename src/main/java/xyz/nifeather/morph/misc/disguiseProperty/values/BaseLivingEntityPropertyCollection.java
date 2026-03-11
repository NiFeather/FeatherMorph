package xyz.nifeather.morph.misc.disguiseProperty.values;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

import java.util.EnumSet;
import java.util.Optional;

public abstract class BaseLivingEntityPropertyCollection<E extends Entity> extends EntityPropertyCollection<E>
{
    public final SingleProperty<Boolean> CUSTOM_NAME_VISIBLE = SingleProperty.builder(PropertyNames.ENTITY_CUSTOM_NAME_VISIBLE, Boolean.class, false)
            .withInputHandle(InputHandles::readBooleanRelaxed)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withSuggestions("true", "false")
            .build();

    public final SingleProperty<Integer> STUCKED_ARROWS = SingleProperty.builder(PropertyNames.ENTITY_ARROW_COUNT, Integer.class, 0)
            .withInputHandle(this::readArrows)
            .withOutputHandle(OutputHandles::writeInteger)
            .build();

    public final SingleProperty<Component> CUSTOM_NAME = SingleProperty.builder(PropertyNames.ENTITY_CUSTOM_NAME, Component.class, Component.empty())
            .withInputHandle(InputHandles::readComponentAny)
            .withOutputHandle(OutputHandles::writeAdventureComponentJSON)
            .withValidator(PropertyValidations::validateCustomTextPermission)
            .build();

    public final SingleProperty<DisguiseEquipment> EQUIPMENT = SingleProperty.builder(PropertyNames.ENTITY_EQUIPMENT, DisguiseEquipment.class, DisguiseEquipment.empty())
            .withInputHandle(InputHandles::readEquipment)
            .withOutputHandle(OutputHandles::writeEquipment)
            .withValidator(PropertyValidations::validateEquipment)
            .hideFromUserInput(true)
            .build();

    public final SingleProperty<Boolean> DISPLAY_DISGUISE_EQUIPMENT = SingleProperty.builder(PropertyNames.ENTITY_DISPLAY_DISGUISE_EQUIPMENT, false)
            .withInputHandle(InputHandles::readBooleanStrict)
            .withOutputHandle(OutputHandles::writeBoolean)
            .build();

    @ApiStatus.Experimental
    public final SingleProperty<Float> STATIC_HEALTH = SingleProperty.builder(PropertyNames.LIVING_ENTITY_STATIC_HEALTH, 1f)
            .withInputHandle(InputHandles::readFloatStrict)
            .withOutputHandle(OutputHandles::writeFloat)
            .hideFromUserInput(true)
            .build();

    @ApiStatus.Experimental
    public final SingleProperty<Vector3i> BED_POS = SingleProperty.builder(PropertyNames.LIVING_ENTITY_BED_POS, new Vector3i(0))
            .withInputHandle(InputHandles::empty)
            .withOutputHandle(OutputHandles::writeVector3i)
            .restoreDefaultsBeforeDiscard(false)
            .hideFromUserInput(true)
            .build();
    
    @ApiStatus.Experimental
    public final SingleProperty<Boolean> INVISIBLE = SingleProperty.builder(PropertyNames.LIVING_ENTITY_INVISIBLE, false)
            .withInputHandle(InputHandles::readBooleanRelaxed)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withValidator(this::validateInvisibility)
            .withSuggestions("true", "false")
            .build();

    private void validateInvisibility(Boolean isInvisible, Entity entity, EnumSet<ValidationFlag> validationFlags)
            throws PropertyValidationException
    {
        if (validationFlags.contains(ValidationFlag.SKIP_PERMISSIONS))
            return;

        if (!entity.hasPermission(CommonPermissions.MAKE_INVISIBLE))
        {
            throw PropertyValidationException.forProperty(PropertyNames.LIVING_ENTITY_INVISIBLE)
                    .withLocalizableMessage(CommandStrings.noPermissionMessage())
                    .withMessage("Player don't have permission to make themselves invisible.")
                    .create();
        }
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

    public BaseLivingEntityPropertyCollection()
    {
        super();

        registerSingle(CUSTOM_NAME, CUSTOM_NAME_VISIBLE, STUCKED_ARROWS, EQUIPMENT, DISPLAY_DISGUISE_EQUIPMENT,
                STATIC_HEALTH, BED_POS, INVISIBLE);
    }
}
