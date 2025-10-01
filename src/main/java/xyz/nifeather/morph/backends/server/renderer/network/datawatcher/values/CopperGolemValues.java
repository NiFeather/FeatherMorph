package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.data.struct.WeatheringCopperState;

public class CopperGolemValues extends MobValues
{
    public final SingleValue<WeatheringCopperState> WEATHERING_STATE = createSingle("copprt_golem_weather_state", WeatheringCopperState.UNAFFECTED, EntityDataTypes.WEATHERING_COPPER_STATE);
    public CopperGolemValues()
    {
        super();
        registerSingle(WEATHERING_STATE);
    }
}
