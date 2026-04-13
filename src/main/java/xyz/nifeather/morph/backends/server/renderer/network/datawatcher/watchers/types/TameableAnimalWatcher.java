package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.utilities.NbtUtils;
import xyz.nifeather.morph.utilities.Uuids;

public class TameableAnimalWatcher extends LivingEntityWatcher
{
    protected TameableAnimalWatcher(IBindTarget bindTarget, EntityType entityType)
    {
        super(bindTarget, entityType);
    }

    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.TAMEABLE);
    }

    protected void writeTamed(boolean tamed)
    {
        byte flag = this.read(ValueIndex.TAMEABLE.TAMEABLE_FLAGS);

        if (tamed)
            writePersistent(ValueIndex.TAMEABLE.TAMEABLE_FLAGS, (byte)(flag | 4));
        else
            writePersistent(ValueIndex.TAMEABLE.TAMEABLE_FLAGS, (byte)(flag & -5));
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var flag = read(ValueIndex.TAMEABLE.TAMEABLE_FLAGS);
        nbt.putBoolean("Sitting", (flag & 0x01) == 0x01);
        NbtUtils.putUUID(nbt, "Owner", read(ValueIndex.TAMEABLE.OWNER).orElse(Uuids.NIL_UUID));
    }
}
