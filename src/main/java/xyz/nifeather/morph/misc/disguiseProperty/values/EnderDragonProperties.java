package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.List;
import java.util.Map;

public class EnderDragonProperties extends BaseLivingEntityProperties<EnderDragon>
{
    public final SingleProperty<Integer> DRAGON_PHASE = getSingle(PropertyNames.ENDER_DRAGON_DRAGON_PHASE, 10);

    public EnderDragonProperties()
    {
        registerSingle(DRAGON_PHASE);
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(PropertyNames.ENDER_DRAGON_DRAGON_PHASE))
            return Pair.of(DRAGON_PHASE, Math.clamp(Integer.parseInt(value), 0, 10));

        return super.parseSingleInput(key, value);
    }

    @Override
    protected @Nullable EnderDragon tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof EnderDragon enderDragon ? enderDragon : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull EnderDragon dragon)
    {
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);
        map.put(DRAGON_PHASE.id(), propertyHandler.get(DRAGON_PHASE) + "");
    }
}
