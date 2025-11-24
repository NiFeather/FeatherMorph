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
import xyz.nifeather.morph.misc.disguiseProperty.values.ZombieVillagerPropertyCollection;

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
        var properties = DisguiseProperties.INSTANCE.getOrThrow(ZombieVillagerPropertyCollection.class);

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

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var villagerData = read(ValueIndex.ZOMBIE_VILLAGER.VILLAGER_DATA);
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
