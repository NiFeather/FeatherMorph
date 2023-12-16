package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;

public class ZombieVillagerValues extends ZombieValues
{
    public final SingleValue<Boolean> CONVERTING = getSingle(false);
    public final SingleValue<VillagerData> VILLAGER_DATA = getSingle(new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));

    public ZombieVillagerValues()
    {
        super();

        registerSingle(CONVERTING, VILLAGER_DATA);
    }
}
