package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.protocol.entity.villager.VillagerData;
import com.github.retrooper.packetevents.protocol.entity.villager.profession.VillagerProfession;
import com.github.retrooper.packetevents.protocol.entity.villager.profession.VillagerProfessions;
import com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerType;
import com.github.retrooper.packetevents.protocol.entity.villager.type.VillagerTypes;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.VillagerProperties;
import xyz.nifeather.morph.utilities.MathUtils;

import java.util.Objects;

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

    // region Cache

    private VillagerProfession profession;
    private VillagerType type;
    private int lvl;

    private VillagerData computeVillagerData()
    {
        var prof = this.profession == null ? VillagerProfessions.NONE : this.profession;
        var type = this.type == null ? VillagerTypes.PLAINS : this.type;

        return new VillagerData(type, prof, lvl);
    }

    // endregion Cache

    @Override
    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
        var properties = DisguiseProperties.INSTANCE.getOrThrow(VillagerProperties.class);

        if (property.equals(properties.LEVEL))
        {
            this.lvl = (Integer) value;

            writePersistent(ValueIndex.ZOMBIE_VILLAGER.VILLAGER_DATA, computeVillagerData());
        }

        if (property.equals(properties.TYPE))
        {
            var bukkitValue = (Villager.Type) value;
            this.type = Objects.requireNonNull(VillagerTypes.getByName(bukkitValue.key().asString()));

            writePersistent(ValueIndex.ZOMBIE_VILLAGER.VILLAGER_DATA, computeVillagerData());
        }

        if (property.equals(properties.PROFESSION))
        {
            var bukkitValue = (Villager.Profession) value;
            this.profession = Objects.requireNonNull(VillagerProfessions.getByName(bukkitValue.key().asString()));

            writePersistent(ValueIndex.ZOMBIE_VILLAGER.VILLAGER_DATA, computeVillagerData());
        }

        super.onPropertyWrite(property, value);
    }

    private void mergeFromVillagerData(CompoundTag nbt)
    {
        int level = 0;
        VillagerProfession profession = VillagerProfessions.NONE;
        VillagerType type = VillagerTypes.PLAINS;

        if (nbt.contains("level"))
            level = MathUtils.clamp(1, 5, nbt.getInt("level").orElseThrow());

        if (nbt.contains("profession"))
        {
            var profString = nbt.getString("profession").orElseThrow();
            var prof = VillagerProfessions.getByName(profString);

            if (prof == null)
                logger.warn("No such profession '%s', using default".formatted(profString));
            else
                profession = prof;
        }

        if (nbt.contains("type"))
        {
            var proftypeString = nbt.getString("type").orElseThrow();

            var typeFromRegistry = VillagerTypes.getByName(proftypeString);

            if (typeFromRegistry == null)
                logger.warn("No such type '%s', using default".formatted(proftypeString));
            else
                type = typeFromRegistry;
        }

        writePersistent(ValueIndex.VILLAGER.VILLAGER_DATA, new VillagerData(type, profession, level));
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
        compound.putString("profession", profession.getName().toString());
        compound.putString("type", type.getName().toString());

        nbt.put("VillagerData", compound);
    }
}
