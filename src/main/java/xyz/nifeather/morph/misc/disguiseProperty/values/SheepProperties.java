package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Sheep;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Arrays;
import java.util.Map;

public class SheepProperties extends BaseLivingEntityProperties<Sheep>
{
    public final SingleProperty<DyeColor> DYE_COLOR = getSingle("sheep/color", DyeColor.getByWoolData((byte)15));

    public SheepProperties()
    {
        DYE_COLOR.withValidInput(Arrays.stream(DyeColor.values()).map(c -> c.name().toLowerCase()).toList());
        registerSingle(DYE_COLOR);
    }

    @Override
    protected @Nullable Sheep tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Sheep sheep ? sheep : null;
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(DYE_COLOR.id()))
        {
            var match = Arrays.stream(DyeColor.values()).filter(v -> v.name().equalsIgnoreCase(value))
                    .findFirst().orElse(null);

            if (match == null)
                return null;

            return Pair.of(DYE_COLOR, match);
        }

        return super.parseSingleInput(key, value);
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Sheep targetEntity)
    {
        if (targetEntity.getColor() != null)
            propertyHandler.set(DYE_COLOR, targetEntity.getColor());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);
        map.put(DYE_COLOR.id(), propertyHandler.get(DYE_COLOR).name().toLowerCase());
    }
}
