package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.utilities.NbtUtils;
import xyz.nifeather.morph.utilities.Uuids;

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
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var flag = read(ValueIndex.TAMEABLE.TAMEABLE_FLAGS);
        nbt.putBoolean("Sitting", (flag & 0x01) == 0x01);
        NbtUtils.putUUID(nbt, "Owner", read(ValueIndex.TAMEABLE.OWNER).orElse(Uuids.NIL_UUID));
    }
}
