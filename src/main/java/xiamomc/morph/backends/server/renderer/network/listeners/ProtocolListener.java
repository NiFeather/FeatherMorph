package xiamomc.morph.backends.server.renderer.network.listeners;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import net.minecraft.world.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.queue.PacketQueue;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.utilities.NmsUtils;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;

public abstract class ProtocolListener extends MorphPluginObject implements PacketListener
{
    @Resolved(shouldSolveImmediately = true)
    private PacketFactory packetFactory;

    @Resolved(shouldSolveImmediately = true)
    private PacketQueue packetQueue;

    public abstract String getIdentifier();

    protected PacketQueue getQueue()
    {
        return packetQueue;
    }

    protected PacketFactory getFactory() { return packetFactory; }

    protected ProtocolManager protocolManager()
    {
        return ProtocolLibrary.getProtocolManager();
    }

    @Override
    public org.bukkit.plugin.Plugin getPlugin()
    {
        return MorphPlugin.getInstance();
    }

    private final Bindable<Boolean> debugOutput = new Bindable<>(false);

    @Initializer
    private void load(MorphConfigManager configManager)
    {
        configManager.bind(debugOutput, ConfigOption.DEBUG_OUTPUT);
    }

    @Nullable
    protected Entity getNmsEntityFrom(PacketEvent event, int id)
    {
        var packetTarget = event.getPlayer();
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

        return sourceNmsEntity;
    }
}
