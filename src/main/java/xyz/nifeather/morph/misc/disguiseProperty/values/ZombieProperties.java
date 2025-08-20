package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zoglin;
import org.bukkit.entity.Zombie;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Map;

public class ZombieProperties extends BaseLivingEntityProperties<Zombie>
{
    public final SingleProperty<Boolean> IS_BABY = getSingle(PropertyNames.ZOMBIE_IS_BABY, false)
            .withValidInput("true", "false");

    public ZombieProperties()
    {
        registerSingle(IS_BABY);
    }

    @Override
    protected @Nullable Zombie tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Zombie zombie ? zombie : null;
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(PropertyNames.ZOMBIE_IS_BABY))
            return Pair.of(IS_BABY, Boolean.valueOf(value));

        return super.parseSingleInput(key, value);
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Zombie targetEntity)
    {
        propertyHandler.set(IS_BABY, !targetEntity.isAdult());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);
        map.put(IS_BABY.id(), propertyHandler.get(IS_BABY).toString().toLowerCase());
    }
}
