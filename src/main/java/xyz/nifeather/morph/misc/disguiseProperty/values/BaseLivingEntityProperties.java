package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

import java.util.Map;
import java.util.Optional;

public abstract class BaseLivingEntityProperties<E extends Entity> extends AbstractProperties<E>
{
    protected SingleProperty<Component> createCustomNameProperty()
    {
        return SingleProperty.of(PropertyNames.ENTITY_CUSTOM_NAME, Component.empty(), Component.class, InputHandles::readComponentAny, OutputHandles::writeAdventureComponentJSON, false);
    }

    public final SingleProperty<Boolean> CUSTOM_NAME_VISIBLE = createProperty(PropertyNames.ENTITY_CUSTOM_NAME_VISIBLE, false, InputHandles::readBooleanRelaxed, OutputHandles::writeBoolean)
            .withValidInput("true", "false");

    public final SingleProperty<Integer> STUCKED_ARROWS = createProperty(PropertyNames.ENTITY_ARROW_COUNT, 0, this::readArrows, OutputHandles::writeInteger);

    public final SingleProperty<Component> CUSTOM_NAME = createCustomNameProperty();

    public final SingleProperty<DisguiseEquipment> EQUIPMENT = SingleProperty.of(PropertyNames.ENTITY_EQUIPMENT, DisguiseEquipment.empty(), InputHandles::readEquipment, OutputHandles::writeEquipment, true);
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

    @Override
    public void validateInput(Map<SingleProperty<?>, Object> result, Player player, boolean ignorePermissions) throws PropertyValidationException
    {
        super.validateInput(result, player, ignorePermissions);

        if (ignorePermissions) return;

        if (result.containsKey(CUSTOM_NAME) && !player.hasPermission(CommonPermissions.DISGUISE_CUSTOM_TEXT))
        {
            throw PropertyValidationException.forProperty(PropertyNames.ENTITY_CUSTOM_NAME)
                    .byMethod("BaseLivingEntityProperties#validateInput")
                    .withLocalizableMessage(CommandStrings.noPermissionMessage())
                    .withMessage("Player don't have permission setting custom name for disguise")
                    .create();
        }

        if (result.getOrDefault(EQUIPMENT, null) instanceof DisguiseEquipment equipment)
        {
            for (ItemStack stack : equipment.contents().values())
            {
                var data = stack.getData(DataComponentTypes.PROFILE);
                if (data != null && !player.hasPermission(CommonPermissions.CUSTOM_SKIN_ON_ITEMS))
                {
                    throw PropertyValidationException.forProperty(PropertyNames.ENTITY_EQUIPMENT)
                            .byMethod("BaseLivingEntityProperties#validateInput")
                            .withLocalizableMessage(CommandStrings.noPermissionMessage())
                            .withMessage("Player don't have permission setting custom skin profile for items")
                            .create();
                }
            }
        }
    }
}
