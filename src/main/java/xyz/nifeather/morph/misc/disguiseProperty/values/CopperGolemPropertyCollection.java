package xyz.nifeather.morph.misc.disguiseProperty.values;

import io.papermc.paper.world.WeatheringCopperState;
import org.bukkit.craftbukkit.entity.CraftCopperGolem;
import org.bukkit.entity.CopperGolem;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Arrays;
import java.util.Optional;

public class CopperGolemPropertyCollection extends BaseLivingEntityPropertyCollection<CopperGolem>
{
    public final SingleProperty<WeatherState> WEATHER_STATE = SingleProperty.builder(PropertyNames.COPPER_GOLEM_WEATHER_STATE, WeatherState.UNAFFECTED)
            .withInputHandle(this::readWeatherState)
            .withOutputHandle(OutputHandles::writeEnum)
            .withSuggestions(Arrays.stream(WeatherState.values()).map(ws -> ws.name().toLowerCase()).toList())
            .build();

    private Optional<WeatherState> readWeatherState(String propertyName, String input) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(WeatherState.values(), propertyName, input);
    }

    public enum WeatherState
    {
        UNAFFECTED,
        EXPOSED,
        WEATHERED,
        OXIDIZED
    }

    public CopperGolemPropertyCollection()
    {
        registerSingle(WEATHER_STATE);
    }

    @Override
    protected @Nullable CopperGolem tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof CopperGolem copperGolem ? copperGolem : null;
    }

    @Override
    public void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull CopperGolem copperGolem)
    {
        super.setupPropertiesFromEntity(propertyHandler, copperGolem);

        var nmsWeatherState = (((CraftCopperGolem)copperGolem).getHandle()).getWeatherState();
        WeatherState bukkitWeatherState = switch (nmsWeatherState)
        {
            case UNAFFECTED -> WeatherState.UNAFFECTED;
            case EXPOSED -> WeatherState.EXPOSED;
            case WEATHERED -> WeatherState.WEATHERED;
            case OXIDIZED -> WeatherState.OXIDIZED;
        };

        propertyHandler.set(WEATHER_STATE, bukkitWeatherState);
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }
}
