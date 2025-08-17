package xyz.nifeather.morph.misc.disguiseProperty.values;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HorseProperties extends BaseLivingEntityProperties<Horse>
{
    private final Map<String, Horse.Color> colorMap = new ConcurrentHashMap<>();
    private final Map<String, Horse.Style> styleMap = new ConcurrentHashMap<>();

    private void initMaps()
    {
        for (var color : Horse.Color.values())
            colorMap.put(color.name().toLowerCase(), color);

        for (var style : Horse.Style.values())
            styleMap.put(style.name().toLowerCase(), style);
    }

    public final SingleProperty<Horse.Color> COLOR = getSingle("horse/color", Horse.Color.WHITE)
            .withRandom(Horse.Color.values());

    public final SingleProperty<Horse.Style> STYLE = getSingle("horse/style", Horse.Style.NONE)
            .withRandom(Horse.Style.values());

    public HorseProperties()
    {
        initMaps();

        COLOR.withValidInput(colorMap.keySet());
        STYLE.withValidInput(styleMap.keySet());

        registerSingle(COLOR, STYLE);
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        switch (key)
        {
            case "horse/color" ->
            {
                var color = colorMap.getOrDefault(value, null);

                if (color != null)
                    return Pair.of(COLOR, color);
            }

            case "horse/style" ->
            {
                var style = styleMap.getOrDefault(value, null);

                if (style != null)
                    return Pair.of(STYLE, style);
            }
        }

        return super.parseSingleInput(key, value);
    }

    @Override
    protected @Nullable Horse tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Horse horse ? horse : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Horse horse)
    {
        propertyHandler.set(COLOR, horse.getColor());
        propertyHandler.set(STYLE, horse.getStyle());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(COLOR, DisguiseUtils.pick(COLOR.getRandomValues()));
        propertyHandler.set(STYLE, DisguiseUtils.pick(STYLE.getRandomValues()));
    }

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);
        map.put(COLOR.id(), propertyHandler.get(COLOR).name().toLowerCase());
        map.put(STYLE.id(), propertyHandler.get(STYLE).name().toLowerCase());
    }
}
