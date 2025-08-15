package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.MathUtils;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SlimeMagmaProperties extends BaseLivingEntityProperties<Slime>
{
    public final SingleProperty<Integer> SIZE = getSingle("slime_magma_size", 1);

    public SlimeMagmaProperties()
    {
        registerSingle(SIZE);
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(SIZE.id()))
        {
            int size = MathUtils.clamp(1, 4, MathUtils.parseIntOr(value, 1));
            return Pair.of(SIZE, size);
        }

        return super.parseSingleInput(key, value);
    }

    @Override
    protected @Nullable Slime tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Slime slime ? slime : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Slime targetEntity)
    {
        propertyHandler.set(SIZE, targetEntity.getSize());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(SIZE, ThreadLocalRandom.current().nextInt(0, 4));
    }

    @Override
    public Map<String, String> mapToNetworkProperties(PropertyHandler propertyHandler)
    {
        return Map.of(
                "size", propertyHandler.getOr(SIZE, 1).toString()
        );
    }
}
