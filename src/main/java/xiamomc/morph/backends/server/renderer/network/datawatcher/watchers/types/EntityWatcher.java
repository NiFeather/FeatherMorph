package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.ValueIndex;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.misc.NmsRecord;

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
        write(values.SILENT, true);
        write(values.NO_GRAVITY, !player.hasGravity());
        write(values.POSE, nmsPlayer.getPose());
        write(values.FROZEN_TICKS, nmsPlayer.getTicksFrozen());

        super.doSync();
    }
}
