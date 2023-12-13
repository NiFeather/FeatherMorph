package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.misc.NmsRecord;

public class EntityWatcher extends SingleWatcher
{
    public EntityWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);

        register(ValueIndex.BASE_ENTITY);
    }

    @Override
    protected void doSync()
    {
        var player = getBindingPlayer();
        var nmsPlayer = NmsRecord.ofPlayer(player);
        var values = ValueIndex.BASE_ENTITY;

        write(values.SILENT, true);
        write(values.NO_GRAVITY, true);
        write(values.POSE, nmsPlayer.getPose());
        write(values.FROZEN_TICKS, nmsPlayer.getTicksFrozen());

        super.doSync();
    }
}
