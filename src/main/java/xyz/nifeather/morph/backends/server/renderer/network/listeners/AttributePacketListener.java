package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateAttributes;
import org.bukkit.NamespacedKey;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;
import xyz.nifeather.morph.utilities.AttributeUtils;
import xyz.nifeather.morph.utilities.NmsUtils;

/**
 * Listener used to remove attributes that's not available for the virtual entity of the disguise.
 */
public class AttributePacketListener extends ProtocolListener
{
    @Override
    public String getIdentifier()
    {
        return "attribute_packet_listener";
    }

    @Resolved(shouldSolveImmediately = true)
    private RenderRegistry registry;

    @Override
    public void onPacketSend(PacketSendEvent event)
    {
        if (event.getPacketType() != PacketType.Play.Server.UPDATE_ATTRIBUTES)
            return;

        var wrapper = new WrapperPlayServerUpdateAttributes(event);

        var sourcePlayer = getPlayerFrom(wrapper.getEntityId());
        if (sourcePlayer == null)
            return;

        var watcher = registry.getWatcher(sourcePlayer.getUniqueId());
        if (watcher == null)
            return;

        var syncableAttributes = AttributeUtils.syncableAttributesFor(watcher.getEntityType());
        var properties = wrapper.getProperties();

        properties.removeIf(property ->
        {
            var keyed = NamespacedKey.fromString(property.getAttribute().getName().toString());

            return watcher.containsEntityAttribute(keyed)
                    || syncableAttributes.stream().noneMatch(a -> a.key().equals(keyed));
        });

        if (properties.isEmpty())
            event.setCancelled(true);

        super.onPacketSend(event);
    }
}
