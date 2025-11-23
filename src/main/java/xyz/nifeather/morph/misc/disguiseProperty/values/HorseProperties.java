package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
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

    public final SingleProperty<Horse.Color> COLOR;

    private Optional<Horse.Color> readHorseColor(String propertyName, String string) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(Horse.Color.values(), propertyName, string);
    }

    public final SingleProperty<Horse.Style> STYLE;

    private Optional<Horse.Style> readHorseStyle(String propertyName, String string) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(Horse.Style.values(), propertyName, string);
    }

    public HorseProperties()
    {
        initMaps();

        COLOR = SingleProperty.builder(PropertyNames.HORSE_COLOR, Horse.Color.WHITE)
                .withInputHandle(this::readHorseColor)
                .withOutputHandle(OutputHandles::writeEnum)
                .withRandom(Horse.Color.values())
                .withSuggestions(colorMap.keySet())
                .build();

        STYLE = SingleProperty.builder(PropertyNames.HORSE_STYLE, Horse.Style.NONE)
                .withInputHandle(this::readHorseStyle)
                .withOutputHandle(OutputHandles::writeEnum)
                .withRandom(Horse.Style.values())
                .withSuggestions(styleMap.keySet())
                .build();

        registerSingle(COLOR, STYLE);
    }

    @Override
    protected @Nullable Horse tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Horse horse ? horse : null;
    }

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Horse horse)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, horse);

        propertyHandler.set(COLOR, horse.getColor());
        propertyHandler.set(STYLE, horse.getStyle());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(COLOR, DisguiseUtils.pick(COLOR.randomValues()));
        propertyHandler.set(STYLE, DisguiseUtils.pick(STYLE.randomValues()));
    }

}
