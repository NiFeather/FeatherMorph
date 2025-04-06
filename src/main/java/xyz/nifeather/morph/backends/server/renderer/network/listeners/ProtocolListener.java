package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.config.ConfigOption;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.utilities.NmsUtils;
import xyz.nifeather.morph.utilities.ReflectionUtils;

import java.lang.reflect.Field;

public abstract class ProtocolListener extends MorphPluginObject implements PacketListener
{
    public abstract String getIdentifier();

    protected PlayerManager playerManager()
    {
        return PacketEvents.getAPI().getPlayerManager();
    }

    private final Bindable<Boolean> debugOutput = new Bindable<>(false);

    protected boolean isDebugEnabled()
    {
        return debugOutput.get();
    }

    @Initializer
    private void load(MorphConfigManager configManager)
    {
        configManager.bind(debugOutput, ConfigOption.DEBUG_OUTPUT);
    }

    protected Player getNmsPlayerEntityFromUnreadablePacket(Packet<?> packet)
    {
        int entityId;

        try
        {
            entityId = ReflectionUtils.getValue(packet, "entityId", int.class, false);
        }
        catch (Throwable t)
        {
            if (isDebugEnabled())
            {
                logger.error("No field 'entityId' in packet " + packet + "! Skipping: " + t.getMessage());

                logger.info("Valid fields: ");
                for (Field declaredField : packet.getClass().getDeclaredFields())
                {
                    logger.info("  \\--" + declaredField.getName());
                }
            }

            return null;
        }

        return this.getNmsPlayerFrom(entityId);
    }

    @Nullable
    protected Player getNmsPlayerFrom(int id)
    {
        //if (!TickThread.isTickThread())
        //    logger.warn("Not on a tick thread! Caution for exceptions!");

        // Bukkit.getOnlinePlayers() 会将正前往不同维度的玩家从列表里移除
        // 因此我们需要在每个世界都手动查询一遍
        for (var world : Bukkit.getWorlds())
        {
            // For performance, we use NMS instead of CraftWorld
            var nmsWorld = NmsUtils.getNmsLevel(world);
            var worldPlayers = nmsWorld.players();

            var match = worldPlayers.stream()
                    .filter(p -> p.getId() == id)
                    .findFirst()
                    .orElse(null);

            if (match != null)
                return match;
        }

        return null;
    }
}
