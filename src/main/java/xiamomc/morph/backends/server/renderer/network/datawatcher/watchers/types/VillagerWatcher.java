package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.misc.disguiseProperty.DisguiseProperties;
import xiamomc.morph.misc.disguiseProperty.SingleProperty;
import xiamomc.morph.misc.disguiseProperty.values.VillagerProperties;
import xiamomc.morph.utilities.MathUtils;

import java.util.Arrays;

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

    // region Cache

    private Villager.Profession profession;
    private Villager.Type type;
    private int lvl;

    private VillagerData computeNmsVillagerData()
    {
        var prof = this.profession == null ? Villager.Profession.NONE : this.profession;
        var type = this.type == null ? Villager.Type.PLAINS : this.type;

        VillagerProfession villagerProfession = VillagerProfession.NONE;
        try
        {
            villagerProfession = BuiltInRegistries.VILLAGER_PROFESSION
                    .getOptional(ResourceLocation.parse(prof.key().asString()))
                    .orElse(VillagerProfession.NONE);
        }
        catch (Throwable t)
        {
            logger.error("Unable to convert bukkit type '%s' to NMS format: " + t.getMessage());
        }

        var availableTypes = Arrays.stream(VillagerTypes.values()).toList();
        var villagerType = availableTypes.get(type.ordinal()).bindingType;

        return new VillagerData(villagerType, villagerProfession, this.lvl);
    }

    // endregion Cache

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(VillagerProperties.class);

        if (property.equals(properties.LEVEL))
        {
            this.lvl = (Integer) value;

            writeOverride(ValueIndex.VILLAGER.VILLAGER_DATA, computeNmsVillagerData());
        }

        if (property.equals(properties.TYPE))
        {
            this.type = (Villager.Type) value;

            writeOverride(ValueIndex.VILLAGER.VILLAGER_DATA, computeNmsVillagerData());
        }

        if (property.equals(properties.PROFESSION))
        {
            this.profession = (Villager.Profession) value;

            writeOverride(ValueIndex.VILLAGER.VILLAGER_DATA, computeNmsVillagerData());
        }

        super.onPropertyWrite(property, value);
    }

    private void mergeFromVillagerData(CompoundTag nbt)
    {
        int level = 0;
        VillagerProfession profession = VillagerProfession.NONE;
        VillagerType type = VillagerType.PLAINS;

        if (nbt.contains("level"))
            level = MathUtils.clamp(1, 5, nbt.getInt("level"));

        if (nbt.contains("profession"))
        {
            ResourceLocation rl = BuiltInRegistries.VILLAGER_PROFESSION.getDefaultKey();

            try
            {
                rl = ResourceLocation.parse(nbt.getString("profession"));
            }
            catch (Throwable t)
            {
                logger.warn("Error occurred while reading nbt to villager profession: " + t.getMessage());
            }

            profession = BuiltInRegistries.VILLAGER_PROFESSION.getOptional(rl).orElse(VillagerProfession.NONE);
        }

        if (nbt.contains("type"))
        {
            ResourceLocation rl = BuiltInRegistries.VILLAGER_TYPE.getDefaultKey();

            try
            {
                rl = ResourceLocation.parse(nbt.getString("type"));
            }
            catch (Throwable t)
            {
                logger.warn("Error occurred while reading nbt to villager type: " + t.getMessage());
            }

            type = BuiltInRegistries.VILLAGER_TYPE.getOptional(rl).orElse(VillagerType.PLAINS);
        }

        writeOverride(ValueIndex.VILLAGER.VILLAGER_DATA, new VillagerData(type, profession, level));
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

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
        compound.putString("profession", BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession).toString());
        compound.putString("type", BuiltInRegistries.VILLAGER_TYPE.getKey(type).toString());

        nbt.put("VillagerData", compound);
    }

    public enum VillagerTypes
    {
        DESERT(VillagerType.DESERT),
        JUNGLE(VillagerType.JUNGLE),
        PLAINS(VillagerType.PLAINS),
        SAVANNA(VillagerType.SAVANNA),
        SNOW(VillagerType.SNOW),
        SWAMP(VillagerType.SWAMP),
        TAIGA(VillagerType.TAIGA);

        public final VillagerType bindingType;

        VillagerTypes(VillagerType bindingType)
        {
            this.bindingType = bindingType;
        }
    }
}
