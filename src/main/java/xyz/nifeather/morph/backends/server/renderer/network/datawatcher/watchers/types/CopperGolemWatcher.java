package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.protocol.entity.data.struct.WeatheringCopperState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.CopperGolemPropertyCollection;

public class CopperGolemWatcher extends LivingEntityWatcher
{
    public CopperGolemWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.COPPER_GOLEM);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.COPPER_GOLEM);
    }

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        if (property.id().equals(PropertyNames.COPPER_GOLEM_WEATHER_STATE))
        {
            CopperGolemPropertyCollection.WeatherState bukkitState = (CopperGolemPropertyCollection.WeatherState) value;
            WeatheringCopperState peState = switch (bukkitState)
            {
                case UNAFFECTED -> WeatheringCopperState.UNAFFECTED;
                case EXPOSED -> WeatheringCopperState.EXPOSED;
                case WEATHERED -> WeatheringCopperState.WEATHERED;
                case OXIDIZED -> WeatheringCopperState.OXIDIZED;
            };

            writePersistent(ValueIndex.COPPER_GOLEM.WEATHERING_STATE, peState);
        }

        super.onPropertyWrite(property, value);
    }
}
