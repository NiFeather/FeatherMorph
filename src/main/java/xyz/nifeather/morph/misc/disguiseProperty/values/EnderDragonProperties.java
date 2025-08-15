package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Map;

public class EnderDragonProperties extends BaseLivingEntityProperties<EnderDragon>
{
    public final SingleProperty<Integer> DRAGON_PHASE = getSingle("dragon_phase", 10);

    public EnderDragonProperties()
    {
        registerSingle(DRAGON_PHASE);
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(DRAGON_PHASE.id()))
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
    public Map<String, String> mapToNetworkProperties(PropertyHandler propertyHandler)
    {
        return Map.of(
                "dragon_phase", propertyHandler.get(DRAGON_PHASE) + ""
        );
    }
}
