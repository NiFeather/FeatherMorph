package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.protocol.entity.villager.VillagerData;
import com.github.retrooper.packetevents.protocol.entity.villager.profession.VillagerProfession;
import com.github.retrooper.packetevents.protocol.entity.villager.profession.VillagerProfessions;
import com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerType;
import com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerTypes;
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

public class VillagerWatcher extends LivingEntityWatcher
{
    public VillagerWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.VILLAGER);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.VILLAGER);
    }

    @Override
    protected void initValues()
    {
        super.initValues();

        var random = new Random();
        var availableTypes = Arrays.stream(VillagerTypes.values()).toList();
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

        logger.info("Merg " + nbt.getAsString());
        if (nbt.contains("VillagerData"))
        {
            var compound = nbt.getCompound("VillagerData");
            mergeFromVillagerData(compound);
        }
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

    public enum VillagerTypes
    {
        DESERT(com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerTypes.DESERT),
        JUNGLE(com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerTypes.JUNGLE),
        PLAINS(com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerTypes.PLAINS),
        SAVANNA(com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerTypes.SAVANNA),
        SNOW(com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerTypes.SNOW),
        SWAMP(com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerTypes.SWAMP),
        TAIGA(com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerTypes.TAIGA);

        public final VillagerType bindingType;

        VillagerTypes(VillagerType bindingType)
        {
            this.bindingType = bindingType;
        }
    }
}
