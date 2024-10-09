package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.VillagerProperties;
import xyz.nifeather.morph.utilities.MathUtils;

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

        var nmsMatch = BuiltInRegistries.VILLAGER_TYPE.getOptional(ResourceLocation.parse(type.getKey().asString()));
        if (nmsMatch.isEmpty())
        {
            logger.warn("Villager type '%s' not found in registry! Ignoring...".formatted(type.getKey().asString()));
            return new VillagerData(VillagerType.PLAINS, villagerProfession, this.lvl);
        }
        else
        {
            return new VillagerData(nmsMatch.get(), villagerProfession, this.lvl);
        }
    }

    // endregion Cache

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(VillagerProperties.class);

        if (property.equals(properties.LEVEL))
        {
            this.lvl = (Integer) value;

            writePersistent(ValueIndex.VILLAGER.VILLAGER_DATA, computeNmsVillagerData());
        }

        if (property.equals(properties.TYPE))
        {
            this.type = (Villager.Type) value;

            writePersistent(ValueIndex.VILLAGER.VILLAGER_DATA, computeNmsVillagerData());
        }

        if (property.equals(properties.PROFESSION))
        {
            this.profession = (Villager.Profession) value;

            writePersistent(ValueIndex.VILLAGER.VILLAGER_DATA, computeNmsVillagerData());
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

        writePersistent(ValueIndex.VILLAGER.VILLAGER_DATA, new VillagerData(type, profession, level));
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

        var villagerData = read(ValueIndex.VILLAGER.VILLAGER_DATA);
        var profession = villagerData.getProfession();
        var type = villagerData.getType();
        var level = villagerData.getLevel();

        var compound = new CompoundTag();
        compound.putInt("level", level);
        compound.putString("profession", BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession).toString());
        compound.putString("type", BuiltInRegistries.VILLAGER_TYPE.getKey(type).toString());

        nbt.put("VillagerData", compound);
    }
}
