package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;

public class ZombieValues extends MonsterValues
{
    public final SingleValue<Boolean> IS_BABY = getSingle("zombie_is_baby", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Integer> ZOMBIE_TYPE = getSingle("zombie_type", 0, EntityDataTypes.INT);
    public final SingleValue<Boolean> CONVERTING_DROWNED = getSingle("zombie_converting_drowned", false, EntityDataTypes.BOOLEAN);

    public ZombieValues()
    {
        super();

        registerSingle(IS_BABY, ZOMBIE_TYPE, CONVERTING_DROWNED);
    }
}
