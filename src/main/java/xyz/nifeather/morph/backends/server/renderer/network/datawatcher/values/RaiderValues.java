package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;

public class RaiderValues extends MonsterValues
{
    public final SingleValue<Boolean> CELEBRATING;

    public RaiderValues()
    {
        CELEBRATING = createSingle("raider_celebrating", false, EntityDataTypes.BOOLEAN);

        registerSingle(CELEBRATING);
    }
}
