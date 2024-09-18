package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes.AbstractVillagerValues;

public class VillagerValues extends AbstractVillagerValues
{
    public final SingleValue<VillagerData> VILLAGER_DATA = createSingle("villager_data", new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 0));

    public VillagerValues()
    {
        super();

        registerSingle(VILLAGER_DATA);
    }
}
