package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.villager.VillagerData;
import com.github.retrooper.packetevents.protocol.entity.villager.profession.VillagerProfessions;
import com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerTypes;

public class ZombieVillagerValues extends ZombieValues
{
    public final SingleValue<Boolean> CONVERTING = createSingle("zVillager_converting", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<VillagerData> VILLAGER_DATA = createSingle("zVillager_data",
            new VillagerData(VillagerTypes.PLAINS, VillagerProfessions.NONE, 0),
            EntityDataTypes.VILLAGER_DATA);

    public ZombieVillagerValues()
    {
        super();

        registerSingle(CONVERTING, VILLAGER_DATA);
    }
}
