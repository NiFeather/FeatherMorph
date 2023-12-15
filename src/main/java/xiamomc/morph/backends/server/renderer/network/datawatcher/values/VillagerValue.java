package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;

public class VillagerValue extends AbstractVillagerValues
{
    public final SingleValue<VillagerData> VILLAGER_DATA = getSingle(new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 0));

    public VillagerValue()
    {
        super();

        registerSingle(VILLAGER_DATA);
    }
}
