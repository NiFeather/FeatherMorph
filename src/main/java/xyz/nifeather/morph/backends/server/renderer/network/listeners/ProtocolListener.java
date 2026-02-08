package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.protocol.player.User;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.BreezeWindCharge;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.api.FeatherMorphAPI;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.VirtualEntity;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;

public abstract class ProtocolListener extends MorphPluginObject implements PacketListener
{
    public abstract String getIdentifier();

    protected PlayerManager playerManager()
    {
        return PacketEvents.getAPI().getPlayerManager();
    }

    @Nullable
    protected LivingEntity getEntityFrom(int id, User viewingUser)
    {
        var viewingPlayer = Bukkit.getPlayer(viewingUser.getUUID());

        if (viewingPlayer == null)
            return null;

        var entity = SpigotConversionUtil.getEntityById(viewingPlayer.getWorld(), id);
        return entity instanceof LivingEntity living ? living : null;
    }

    @Resolved(shouldSolveImmediately = true)
    private RenderRegistry registry;

    protected void handleException(@Nullable Entity source, VirtualEntity watcher, Throwable t)
    {
        boolean handled = false;
        var api = FeatherMorphAPI.instance();

        // Sometimes API would return NULL where I believe it shouldn't... D:
        if (api != null)
        {
            var state = api.directAccess().morphManager().getDisguiseStateFor(source);
            if (state != null)
            {
                logger.info("Failed processing packet, calling DisguiseState#handleException");
                state.handleException(t);
                handled = true;
            }
        }

        if (!handled)
        {
            // If API is not ready (where it shouldn't), unregister from render registry to prevent future chaos
            logger.error("Unhandled exception when processing packet", t);
            registry.unregister(watcher);
        }
    }
}
