package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Entity;
import org.bukkit.entity.MushroomCow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;

public class MooshroomProperties extends BaseLivingEntityProperties<MushroomCow>
{
    public final SingleProperty<MushroomCow.Variant> VARIANT = getSingle("mooshroom/variant", MushroomCow.Variant.RED)
            .withRandom(MushroomCow.Variant.RED, MushroomCow.Variant.RED, MushroomCow.Variant.RED, MushroomCow.Variant.BROWN)
            .withValidInput("red", "brown");

    public MooshroomProperties()
    {
        registerSingle(VARIANT);
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(VARIANT.id()))
        {
            if (value.equals("red"))
                return Pair.of(VARIANT, MushroomCow.Variant.RED);
            else if (value.equals("brown"))
                return Pair.of(VARIANT, MushroomCow.Variant.BROWN);
        }

        return super.parseSingleInput(key, value);
    }

    @Override
    protected @Nullable MushroomCow tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof MushroomCow mushroomCow ? mushroomCow : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull MushroomCow targetEntity)
    {
        propertyHandler.set(VARIANT, targetEntity.getVariant());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(VARIANT, DisguiseUtils.pick(VARIANT.getRandomValues()));
    }

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);
        map.put(VARIANT.id(), propertyHandler.get(VARIANT).name().toLowerCase());
    }
}
