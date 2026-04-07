package xyz.nifeather.morph.misc.attributes;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.key.Key;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MorphAttributeInstance implements AttributeInstance
{
    private final @NotNull Attribute attribute;
    private final Map<Key, AttributeModifier> attributeModifierMap = new ConcurrentHashMap<>();
    private volatile double baseValue;
    private double cachedValue = 0d;
    private boolean dirty = false;

    public MorphAttributeInstance(MorphAttributeInstance other)
    {
        this(other.attribute);
        this.baseValue = other.baseValue;
        this.attributeModifierMap.putAll(other.attributeModifierMap);
        this.dirty = other.dirty;
        this.cachedValue = other.cachedValue;
    }

    public MorphAttributeInstance(@NotNull Attribute attribute)
    {
        this.attribute = attribute;
    }

    /**
     * The attribute pertaining to this instance.
     *
     * @return the attribute
     */
    @Override
    public @NotNull Attribute getAttribute()
    {
        return this.attribute;
    }


    /**
     * Base value of this instance before modifiers are applied.
     *
     * @return base value
     */
    @Override
    public double getBaseValue()
    {
        return baseValue;
    }

    /**
     * Set the base value of this instance.
     *
     * @param value new base value
     */
    @Override
    public void setBaseValue(double value)
    {
        baseValue = value;
        dirty = true;
    }

    /**
     * Get all modifiers present on this instance.
     *
     * @return a copied collection of all modifiers
     */
    @Override
    public @NotNull Collection<AttributeModifier> getModifiers()
    {
        return attributeModifierMap.values();
    }

    /**
     * Gets the modifier with the corresponding key.
     *
     * @param key the key of the modifier
     * @return the modifier, if it exists
     */
    @Override
    public @Nullable AttributeModifier getModifier(@NotNull Key key)
    {
        return attributeModifierMap.getOrDefault(key, null);
    }

    /**
     * Remove a modifier with the corresponding key from this instance.
     *
     * @param key the key of the modifier
     */
    @Override
    public void removeModifier(@NotNull Key key)
    {
        attributeModifierMap.remove(key);
        dirty = true;
    }

    /**
     * Gets the modifier with the corresponding UUID.
     *
     * @param uuid the UUID of the modifier
     * @return the modifier, if it exists
     * @deprecated use {@link #getModifier(Key)}, modifiers are no longer stored by UUID
     */
    @Override
    @SuppressWarnings("removal")
    public @Nullable AttributeModifier getModifier(@NotNull UUID uuid)
    {
        throw new IllegalArgumentException("We don't accept UUIDs, sorry.");
    }

    /**
     * Remove a modifier with the corresponding UUID from this instance.
     *
     * @param uuid the UUID of the modifier
     * @deprecated use {@link #removeModifier(Key)}, modifiers are no longer stored by UUID
     */
    @Override
    @SuppressWarnings("removal")
    public void removeModifier(@NotNull UUID uuid)
    {
        throw new IllegalArgumentException("We don't accept UUIDs, sorry.");
    }

    /**
     * Add a modifier to this instance.
     *
     * @param modifier to add
     */
    @Override
    public void addModifier(@NotNull AttributeModifier modifier)
    {
        attributeModifierMap.put(modifier.getKey(), modifier);
        dirty = true;
    }

    /**
     * Add a transient modifier to this instance.
     * Transient modifiers are not persisted (saved with the NBT data)
     *
     * @param modifier to add
     */
    @Override
    public void addTransientModifier(@NotNull AttributeModifier modifier)
    {
        attributeModifierMap.put(modifier.getKey(), modifier);
        dirty = true;
    }

    /**
     * Remove a modifier from this instance.
     *
     * @param modifier to remove
     */
    @Override
    public void removeModifier(@NotNull AttributeModifier modifier)
    {
        attributeModifierMap.remove(modifier.getKey());
        dirty = true;
    }

    public boolean isDirty()
    {
        return dirty;
    }

    public double reCalculateValue()
    {
        double value = this.baseValue;

        List<AttributeModifier> modifiers_AddValue = new ObjectArrayList<>();
        List<AttributeModifier> modifiers_MultiplyBase = new ObjectArrayList<>();
        List<AttributeModifier> modifiers_MultiplyValue = new ObjectArrayList<>();

        var modifiers = attributeModifierMap.values();
        for (AttributeModifier modifier : modifiers)
        {
            switch(modifier.getOperation())
            {
                case ADD_NUMBER -> modifiers_AddValue.add(modifier);
                case ADD_SCALAR -> modifiers_MultiplyBase.add(modifier);
                case MULTIPLY_SCALAR_1 -> modifiers_MultiplyValue.add(modifier);
            }
        }

        for (AttributeModifier modifier : modifiers_AddValue)
            value = value + modifier.getAmount();

        double multiplyAmount = 0;
        for (AttributeModifier modifier : modifiers_MultiplyBase)
            multiplyAmount += modifier.getAmount();

        value *= (1 + multiplyAmount);

        for (AttributeModifier modifier : modifiers_MultiplyValue)
            value *= (1 + modifier.getAmount());

        this.cachedValue = value;
        dirty = false;

        return value;
    }

    /**
     * Get the value of this instance after all associated modifiers have been
     * applied.
     *
     * @return the total attribute value
     */
    @Override
    public double getValue()
    {
        if (!dirty)
            return cachedValue;

        return reCalculateValue();
    }

    /**
     * Gets the default value of the Attribute attached to this instance.
     *
     * @return server default value
     */
    @Override
    public double getDefaultValue()
    {
        return 1;
    }

    public static MorphAttributeInstance copy(MorphAttributeInstance other)
    {
        return new MorphAttributeInstance(other);
    }
}
