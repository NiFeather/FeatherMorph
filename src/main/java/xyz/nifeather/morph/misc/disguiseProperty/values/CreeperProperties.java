package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;

public class CreeperProperties extends BaseLivingEntityProperties<Creeper>
{
    public final SingleProperty<Boolean> CHARGED = getSingle(PropertyNames.CREEPER_CHARGED, false)
            .withRandom(false, false, false, true)
            .withValidInput("true", "false");

    public CreeperProperties()
    {
        registerSingle(CHARGED);
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(PropertyNames.CREEPER_CHARGED))
            return Pair.of(CHARGED, Boolean.valueOf(value));

        return super.parseSingleInput(key, value);
    }

    @Override
    protected @Nullable Creeper tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Creeper creeper ? creeper : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Creeper creeper)
    {
        propertyHandler.set(CHARGED, creeper.isPowered());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(CHARGED, DisguiseUtils.pick(CHARGED.getRandomValues()));
    }

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);
        map.put(CHARGED.id(), propertyHandler.get(CHARGED).toString().toLowerCase());
    }
}
