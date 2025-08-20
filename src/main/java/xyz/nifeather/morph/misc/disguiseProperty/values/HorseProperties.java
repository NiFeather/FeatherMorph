package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.Optional;
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

    public final SingleProperty<Horse.Color> COLOR = getSingle(PropertyNames.HORSE_COLOR, Horse.Color.WHITE, this::readHorseColor)
            .withRandom(Horse.Color.values());

    private Optional<Horse.Color> readHorseColor(String string) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(Horse.Color.values(), string);
    }

    public final SingleProperty<Horse.Style> STYLE = getSingle(PropertyNames.HORSE_STYLE, Horse.Style.NONE, this::readHorseStyle)
            .withRandom(Horse.Style.values());

    private Optional<Horse.Style> readHorseStyle(String string) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(Horse.Style.values(), string);
    }

    public HorseProperties()
    {
        initMaps();

        COLOR.withValidInput(colorMap.keySet());
        STYLE.withValidInput(styleMap.keySet());

        registerSingle(COLOR, STYLE);
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
