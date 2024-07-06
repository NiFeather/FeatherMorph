package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.protocol.entity.villager.VillagerData;
import com.github.retrooper.packetevents.protocol.entity.villager.profession.VillagerProfession;
import com.github.retrooper.packetevents.protocol.entity.villager.profession.VillagerProfessions;
import com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.utilities.MathUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class ZombieVillagerWatcher extends ZombieWatcher
{
    public ZombieVillagerWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.ZOMBIE_VILLAGER);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.ZOMBIE_VILLAGER);
    }

    @Override
    protected void initValues()
    {
        super.initValues();

        var random = new Random();
        var availableTypes = Arrays.stream(VillagerWatcher.VillagerTypes.values()).toList();
        var villagerType = availableTypes.get(random.nextInt(availableTypes.size())).bindingType;

        var availableProfessions = Arrays.stream(Villager.Profession.values()).toList();
        var targetBukkit = availableProfessions.get(random.nextInt(availableProfessions.size())).getKey().asString();
        VillagerProfession villagerProfession = VillagerProfessions.NONE;

        try
        {
            VillagerProfessions.getByName(targetBukkit);
        }
        catch (Throwable t)
        {
            logger.error("Unable to convert bukkit type '%s' to PacketEvent format: " + t.getMessage());
        }

        var level = random.nextInt(1, 6);
        write(ValueIndex.VILLAGER.VILLAGER_DATA, new VillagerData(villagerType, villagerProfession, level));
    }

    private void mergeFromVillagerData(CompoundTag nbt)
    {
        int level = 0;
        VillagerProfession profession = VillagerProfessions.NONE;
        VillagerType type = com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerTypes.PLAINS;

        if (nbt.contains("level"))
            level = MathUtils.clamp(1, 5, nbt.getInt("level"));

        if (nbt.contains("profession"))
        {
            try
            {
                var prof = VillagerProfessions.getByName(nbt.getString("profession"));
                profession = Objects.requireNonNull(prof, "No such profession '%s'".formatted(nbt.getString("profession")));
            }
            catch (Throwable t)
            {
                logger.warn("Error occurred while reading nbt to villager profession: " + t.getMessage());
            }
        }

        if (nbt.contains("type"))
        {
            try
            {
                var newType = com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerTypes.getByName(nbt.getString("type"));
                type = Objects.requireNonNull(newType, "No such type '%s'".formatted(nbt.getString("type")));
            }
            catch (Throwable t)
            {
                logger.warn("Error occurred while reading nbt to villager type: " + t.getMessage());
            }
        }

        write(ValueIndex.VILLAGER.VILLAGER_DATA, new VillagerData(type, profession, level));
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("VillagerData"))
            mergeFromVillagerData(nbt.getCompound("VillagerData"));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var villagerData = get(ValueIndex.VILLAGER.VILLAGER_DATA);
        var profession = villagerData.getProfession();
        var type = villagerData.getType();
        var level = villagerData.getLevel();

        var compound = new CompoundTag();
        compound.putInt("level", level);
        compound.putString("profession", profession.getName().toString());
        compound.putString("type", type.getName().toString());

        nbt.put("VillagerData", compound);
    }
}
