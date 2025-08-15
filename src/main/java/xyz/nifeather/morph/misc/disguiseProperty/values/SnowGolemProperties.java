package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Snowman;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types.SnowGolemWatcher;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Map;

public class SnowGolemProperties extends BaseLivingEntityProperties<Snowman>
{
    public final SingleProperty<Boolean> HAS_PUMPKIN = getSingle("has_pumpkin", true);

    public SnowGolemProperties()
    {
        registerSingle(HAS_PUMPKIN);
    }

    @Override
    protected @Nullable Snowman tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Snowman snowman ? snowman : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Snowman targetEntity)
    {
        propertyHandler.set(HAS_PUMPKIN, !targetEntity.isDerp());
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(HAS_PUMPKIN.id()))
        {
            return Pair.of(HAS_PUMPKIN, Boolean.valueOf(value));
        }

        return super.parseSingleInput(key, value);
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

    @Override
    public Map<String, String> mapToNetworkProperties(PropertyHandler propertyHandler)
    {
        return Map.of(
                "has_pumpkin", propertyHandler.get(HAS_PUMPKIN).toString().toLowerCase()
        );
    }
}
