package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;

public class TameableAnimalWatcher extends LivingEntityWatcher
{
    protected TameableAnimalWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.TAMEABLE);
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Owner"))
        {
            write(ValueIndex.TAMEABLE.OWNER, nbt.getUUID("Owner"));

            byte val = get(ValueIndex.TAMEABLE.TAMEABLE_FLAGS);
            write(ValueIndex.TAMEABLE.TAMEABLE_FLAGS, (byte)(val | 0x04));
        }

        if (nbt.contains("Sitting"))
        {
            byte val = get(ValueIndex.TAMEABLE.TAMEABLE_FLAGS);

            write(ValueIndex.TAMEABLE.TAMEABLE_FLAGS, (byte)(val | 0x01));
        }
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var flag = get(ValueIndex.TAMEABLE.TAMEABLE_FLAGS);
        nbt.putBoolean("Sitting", (flag & 0x01) == 0x01);
        nbt.putUUID("Owner", get(ValueIndex.TAMEABLE.OWNER));
    }
}
