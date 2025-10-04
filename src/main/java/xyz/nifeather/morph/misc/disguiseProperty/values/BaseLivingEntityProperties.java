package xyz.nifeather.morph.misc.disguiseProperty.values;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.messages.CommandStrings;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

import java.util.Map;
import java.util.Optional;

public abstract class BaseLivingEntityProperties<E extends Entity> extends AbstractProperties<E>
{
    protected SingleProperty<Component> createCustomNameProperty()
    {
        return createProperty(PropertyNames.ENTITY_CUSTOM_NAME, Component.empty(), InputHandles::readComponentAny, OutputHandles::writeAdventureComponentJSON);
    }

    public final SingleProperty<Boolean> CUSTOM_NAME_VISIBLE = createProperty(PropertyNames.ENTITY_CUSTOM_NAME_VISIBLE, false, InputHandles::readBooleanRelaxed, OutputHandles::writeBoolean)
            .withValidInput("true", "false");

    public final SingleProperty<Integer> STUCKED_ARROWS = createProperty(PropertyNames.ENTITY_ARROW_COUNT, 0, this::readArrows, OutputHandles::writeInteger);

    public final SingleProperty<Component> CUSTOM_NAME = createCustomNameProperty();

    public final SingleProperty<DisguiseEquipment> EQUIPMENT = SingleProperty.of(PropertyNames.ENTITY_EQUIPMENT, DisguiseEquipment.empty(), InputHandles::reservedException, OutputHandles::writeEquipment, true);
    public final SingleProperty<Boolean> DISPLAY_DISGUISE_EQUIPMENT = SingleProperty.of(PropertyNames.ENTITY_DISPLAY_DISGUISE_EQUIPMENT, false, InputHandles::reservedException, OutputHandles::writeBoolean, true);

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

    @Override
    public void validateInput(Map<SingleProperty<?>, Object> result, Player player) throws PropertyValidationException
    {
        if (result.containsKey(CUSTOM_NAME) && !player.hasPermission(CommonPermissions.DISGUISE_CUSTOM_TEXT))
        {
            throw PropertyValidationException.forProperty(PropertyNames.MANNEQUIN_SKIN)
                    .byMethod("BaseLivingEntityProperties#validateInput")
                    .withLocalizableMessage(CommandStrings.noPermissionMessage())
                    .withMessage("Player don't have permission for setting custom text")
                    .create();
        }

        super.validateInput(result, player);
    }
}
