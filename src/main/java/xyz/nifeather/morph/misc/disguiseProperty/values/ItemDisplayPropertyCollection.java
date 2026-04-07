package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Arrays;
import java.util.Optional;

public class ItemDisplayPropertyCollection extends DisplayEntityPropertyCollection<ItemDisplay>
{
    public final SingleProperty<ItemStack> DISPLAYING_ITEM = SingleProperty.builder(PropertyNames.ITEM_DISPLAY_DISPLAYING_ITEM, ItemStack.class, ItemStack.of(Material.STONE))
            .withInputHandle(InputHandles::readItemTypedOrStack)
            .withOutputHandle(OutputHandles::writeItemStack)
            .build();

    public final SingleProperty<ItemDisplay.ItemDisplayTransform> DISPLAY_MODEL = SingleProperty.builder(PropertyNames.ITEM_DISPLAY_MODEL_TRANSFORM, ItemDisplay.ItemDisplayTransform.FIXED)
            .withInputHandle(this::readItemDisplayTransform)
            .withOutputHandle(OutputHandles::writeEnum)
            .withSuggestions(Arrays.stream(ItemDisplay.ItemDisplayTransform.values()).map(e -> e.toString().toLowerCase()).toList())
            .build();

    private Optional<ItemDisplay.ItemDisplayTransform> readItemDisplayTransform(String propertyName, String input)
            throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(ItemDisplay.ItemDisplayTransform.values(), propertyName, input);
    }

    public ItemDisplayPropertyCollection()
    {
        registerSingle(DISPLAYING_ITEM, DISPLAY_MODEL);
    }

    @Override
    protected @Nullable ItemDisplay tryCastEntity(@org.jetbrains.annotations.Nullable Entity targetEntity)
    {
        return targetEntity instanceof ItemDisplay display ? display : null;
    }

    /**
     * Setup property for the given disguise from the given entity
     *
     * @param propertyHandler The {@link PropertyHandler} for the given disguise
     * @param targetEntity    The targeted entity
     */
    @Override
    public void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NonNull ItemDisplay targetEntity)
    {
        super.setupPropertiesFromEntity(propertyHandler, targetEntity);

        propertyHandler.set(DISPLAYING_ITEM, targetEntity.getItemStack());
        propertyHandler.set(DISPLAY_MODEL, targetEntity.getItemDisplayTransform());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(DISPLAY_MODEL, ItemDisplay.ItemDisplayTransform.FIXED);
    }
}
