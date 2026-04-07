package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HappyGhast;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.messages.strings.ExceptionStrings;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Arrays;

public class HappyGhastPropertyCollection extends BaseLivingEntityPropertyCollection<HappyGhast>
{
    public final SingleProperty<Boolean> IS_GHASTLING = SingleProperty.builder(PropertyNames.HAPPY_GHAST_IS_GHASTLING, false)
            .withInputHandle(InputHandles::readBooleanRelaxed)
            .withOutputHandle(OutputHandles::writeBoolean)
            .withSuggestions("true", "false")
            .build();

    public final SingleProperty<DyeColor> HARNESS = SingleProperty.builder(PropertyNames.HAPPY_GHAST_HARNESS, DyeColor.BLACK)
            .withInputHandle(InputHandles::readDyeColor)
            .withOutputHandle(OutputHandles::writeEnum)
            .withSuggestions(Arrays.stream(DyeColor.values()).map(c -> c.name().toLowerCase()).toList())
            .withPostProcess(this::handleSaddle)
            .build();

    private void handleSaddle(DyeColor dyeColor, PropertyHandler propertyHandler)
            throws ParseErrorException
    {
        var id = NamespacedKey.fromString("%s_harness".formatted(dyeColor.name().toLowerCase()));
        if (id == null)
        {
            throw ParseErrorException.forProperty(PropertyNames.HAPPY_GHAST_HARNESS)
                    .withLocalizableMessage(ExceptionStrings.malformedInput())
                    .withMessage("Cannot make '%s_harness' as an Identifier, bad server implementation?".formatted(dyeColor.name().toLowerCase()))
                    .create();
        }

        var item = Registry.ITEM.get(id);
        if (item == null)
        {
            throw ParseErrorException.forProperty(PropertyNames.HAPPY_GHAST_HARNESS)
                    .withLocalizableMessage(ExceptionStrings.noValueMatch())
                    .withMessage("Cannot find item with name %s".formatted(id.asString()))
                    .create();
        }

        var equipment = propertyHandler.get(EQUIPMENT)
                .cloneForEdit()
                .forSlot(EquipmentSlot.BODY, item.createItemStack())
                .build();

        propertyHandler.set(EQUIPMENT, equipment);
        propertyHandler.set(DISPLAY_DISGUISE_EQUIPMENT, true);
    }

    public HappyGhastPropertyCollection()
    {
        registerSingle(IS_GHASTLING, HARNESS);
    }

    @Override
    protected @Nullable HappyGhast tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof HappyGhast happyGhast ? happyGhast : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull HappyGhast happyGhast)
    {
        super.setupPropertiesFromEntity(propertyHandler, happyGhast);

        propertyHandler.set(IS_GHASTLING, !happyGhast.isAdult());

        var saddleItem = happyGhast.getEquipment().getItem(EquipmentSlot.BODY);
        if (!saddleItem.getType().isAir() && !saddleItem.isEmpty())
        {
            var equipment = propertyHandler.get(EQUIPMENT)
                    .cloneForEdit()
                    .forSlot(EquipmentSlot.BODY, saddleItem)
                    .build();

            propertyHandler.set(EQUIPMENT, equipment);
            propertyHandler.set(DISPLAY_DISGUISE_EQUIPMENT, true);
        }
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(IS_GHASTLING, false);
    }

}
