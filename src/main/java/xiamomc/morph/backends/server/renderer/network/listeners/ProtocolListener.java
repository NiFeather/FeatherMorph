package xiamomc.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.*;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.ProtocolHandler;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.utilities.NmsUtils;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.function.Function;

public abstract class ProtocolListener extends MorphPluginObject
{
    public static class ListenerWrapper extends PacketListenerAbstract
    {
        private final ProtocolListener listener;

        public ListenerWrapper(ProtocolListener protocolListener)
        {
            this.listener = protocolListener;
        }

        @Override
        public void onPacketSend(PacketSendEvent event)
        {
            super.onPacketSend(event);

            this.listener.onPacketSending(event);
        }
    }

    private final ListenerWrapper listenerWrapper;

    public ListenerWrapper listenerWrapper()
    {
        return this.listenerWrapper;
    }

    protected ProtocolListener()
    {
        this.listenerWrapper = new ListenerWrapper(this);
    }

    @Resolved(shouldSolveImmediately = true)
    private PacketFactory packetFactory;

    public abstract String getIdentifier();

    protected PacketFactory getFactory() { return packetFactory; }

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

    protected abstract void onPacketSending(PacketSendEvent e);

    @Nullable
    protected Player getNmsPlayerEntityFrom(int id)
    {
        //if (!TickThread.isTickThread())
        //    logger.warn("Not on a tick thread! Caution for exceptions!");

        // Bukkit.getOnlinePlayers() 会将正前往不同维度的玩家从列表里移除
        // 因此我们需要在每个世界都手动查询一遍
        for (var world : Bukkit.getWorlds())
        {
            var worldPlayers = world.getPlayers();

            var match = worldPlayers.stream()
                    .filter(p -> p.getEntityId() == id)
                    .map(bukkit -> ((CraftPlayer)bukkit).getHandle())
                    .map(Optional::ofNullable)
                    .findFirst().flatMap(Function.identity())
                    .orElse(null);

            if (match != null)
                return match;
        }

        return null;
    }

    @Nullable
    protected Entity getNmsEntityFrom(ProtocolPacketEvent<?> event, int id)
    {
        var entity = SpigotReflectionUtil.getEntityById(id);
        if (entity == null) return null;
        else return ((CraftEntity)entity).getHandleRaw();
/*
        var packetTarget = (org.bukkit.entity.Player) event.getPlayer();
        var sourceNmsEntity = NmsUtils.getNmsLevel(packetTarget.getWorld()).getEntity(id);
        if (sourceNmsEntity == null)
        {
            if (debugOutput.get())
            {
                logger.warn("A packet from a player that doesn't exist in its world?!");
                logger.warn("Packet: " + event.getPacketType());
            }

            return null;
        }

        return sourceNmsEntity;*/
    }
}
