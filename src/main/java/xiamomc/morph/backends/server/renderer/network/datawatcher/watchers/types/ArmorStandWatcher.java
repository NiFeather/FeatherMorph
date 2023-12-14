package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.pluginbase.Annotations.Resolved;

public class ArmorStandWatcher extends InventoryLivingWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.ARMOR_STAND);
    }

    public ArmorStandWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.ARMOR_STAND);
    }


    public byte getArmorStandFlags(boolean small, boolean showArms, boolean noBasePlate)
    {
        var value = (byte)0x00;

        if (small)
            value |= (byte)0x01;

        if (showArms)
            value |= (byte)0x04;

        if (noBasePlate)
            value |= (byte)0x08;

        return value;
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        var small = nbt.getBoolean("Small");
        var noBasePlate = nbt.getBoolean("NoBasePlate");
        var showArms = nbt.getBoolean("ShowArms");
        write(ValueIndex.ARMOR_STAND.DATA_FLAGS, getArmorStandFlags(small, showArms, noBasePlate));
    }
}
