package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.settings.PacketEventsSettings;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.config.ConfigOption;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.misc.NmsRecord;
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

    @Nullable
    protected Player getNmsPlayerFrom(int id)
    {
        //if (!TickThread.isTickThread())
        //    logger.warn("Not on a tick thread! Caution for exceptions!");

        var bukkitPlayer = Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getEntityId() == id)
                .findFirst()
                .orElse(null);

        if (bukkitPlayer == null)
            return null;

        return NmsRecord.ofPlayer(bukkitPlayer);

        // Bukkit.getOnlinePlayers() 会将正前往不同维度的玩家从列表里移除
        // 因此我们需要在每个世界都手动查询一遍
        // 2024/4/7: Seems no longer an issue after migrate to packetevents.
        /*for (var world : Bukkit.getWorlds())
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

        return null;*/
    }
}
