package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.misc.NmsRecord;

import java.util.Optional;

public class EntityWatcher extends SingleWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.BASE_ENTITY);
    }

    public EntityWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);
    }

    @Override
    protected void doSync()
    {
        super.doSync();

        var player = getBindingPlayer();
        var nmsPlayer = NmsRecord.ofPlayer(player);
        var values = ValueIndex.BASE_ENTITY;

        byte bitMask = 0x00;
        if (player.getFireTicks() > 0 || player.isVisualFire())
            bitMask |= (byte) 0x01;

        if (player.isSneaking())
            bitMask |= (byte) 0x02;

        if (player.isSprinting())
            bitMask |= (byte) 0x08;

        if (player.isSwimming())
            bitMask |= (byte) 0x10;

        if (player.isInvisible())
            bitMask |= (byte) 0x20;

        if (player.isGlowing())
            bitMask |= (byte) 0x40;

        if (NmsRecord.ofPlayer(player).isFallFlying())
            bitMask |= (byte) 0x80;

        write(values.GENERAL, bitMask);
        //write(values.SILENT, true);
        write(values.NO_GRAVITY, !player.hasGravity());
        write(values.POSE, SpigotConversionUtil.fromBukkitPose(player.getPose()));
        write(values.FROZEN_TICKS, nmsPlayer.getTicksFrozen());
    }

    @Override
    protected <X> void onCustomWrite(RegistryKey<X> key, X oldVal, X newVal)
    {
        super.onCustomWrite(key, oldVal, newVal);

        if (key.equals(EntryIndex.DISGUISE_NAME))
        {
            var str = newVal.toString();
            var component = str.isEmpty() ? null : Component.text(str);
            write(ValueIndex.BASE_ENTITY.CUSTOM_NAME, component == null ? Optional.empty() : Optional.of(component));
        }
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("CustomName"))
        {
            Component component = null;

            try
            {
                component = MiniMessage.miniMessage().deserializeOrNull(nbt.getString("CustomName"));
            }
            catch (Throwable t)
            {
                logger.warn("Can't deserialize component: " + t.getMessage());
            }

            if (component != null)
                write(ValueIndex.BASE_ENTITY.CUSTOM_NAME, Optional.of(component));
        }

        if (nbt.contains("CustomNameVisible"))
        {
            var visible = nbt.getBoolean("CustomNameVisible");
            write(ValueIndex.BASE_ENTITY.CUSTOM_NAME_VISIBLE, visible);
        }
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var customName = get(ValueIndex.BASE_ENTITY.CUSTOM_NAME);

        try
        {
            customName.ifPresent(c -> nbt.putString("CustomName", MiniMessage.miniMessage().serialize(c)));
        }
        catch (Throwable t)
        {
            logger.error("Can't serialize component: " + t.getMessage());
        }

        nbt.putBoolean("CustomNameVisible", get(ValueIndex.BASE_ENTITY.CUSTOM_NAME_VISIBLE));
    }
}
