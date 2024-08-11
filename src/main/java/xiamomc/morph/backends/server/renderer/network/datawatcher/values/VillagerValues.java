package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.villager.VillagerData;
import com.github.retrooper.packetevents.protocol.entity.villager.profession.VillagerProfessions;
import com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerTypes;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AbstractVillagerValues;

public class VillagerValues extends AbstractVillagerValues
{
    public final SingleValue<VillagerData> VILLAGER_DATA = getSingle("villager_data", new VillagerData(VillagerTypes.PLAINS, VillagerProfessions.NONE, 0), EntityDataTypes.VILLAGER_DATA);

    public VillagerValues()
    {
        super();

        registerSingle(VILLAGER_DATA);
    }
}
