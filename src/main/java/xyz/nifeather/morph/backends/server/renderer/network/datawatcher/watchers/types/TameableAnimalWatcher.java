package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.utilities.NbtUtils;

import java.util.Optional;
import java.util.UUID;

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

    private final UUID ownerUUID = UUID.randomUUID();

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("Owner") && !Util.NIL_UUID.equals(NbtUtils.readUUID(nbt.get("Owner"))))
        {
            writePersistent(ValueIndex.TAMEABLE.OWNER, Optional.of(ownerUUID));

            byte val = read(ValueIndex.TAMEABLE.TAMEABLE_FLAGS);
            writePersistent(ValueIndex.TAMEABLE.TAMEABLE_FLAGS, (byte)(val | 0x04));
        }

        if (nbt.contains("Sitting"))
        {
            byte val = read(ValueIndex.TAMEABLE.TAMEABLE_FLAGS);

            if (nbt.getBoolean("Sitting").orElseThrow())
                writePersistent(ValueIndex.TAMEABLE.TAMEABLE_FLAGS, (byte)(val | 0x01));
        }
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var flag = read(ValueIndex.TAMEABLE.TAMEABLE_FLAGS);
        nbt.putBoolean("Sitting", (flag & 0x01) == 0x01);
        NbtUtils.putUUID(nbt, "Owner", read(ValueIndex.TAMEABLE.OWNER).orElse(Util.NIL_UUID));
    }
}
