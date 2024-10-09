package xyz.nifeather.morph.backends.server.renderer.network.queue;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class QueueEntry
{
    @Nullable
    public final Consumer<ProtocolManager> action;
    public final PacketContainer packet;
    public final int delay;

    long targetTick;

    public QueueEntry(PacketContainer packetContainer, int delay)
    {
        this(packetContainer, null, delay);
    }

    public QueueEntry(PacketContainer packet, Consumer<ProtocolManager> action, int delay)
    {
        this.packet = packet;
        this.action = action;
        this.delay = delay;
    }

    public static QueueEntry from(PacketContainer packet)
    {
        return from(packet, 0);
    }

    public static QueueEntry from(PacketContainer packet, int delay)
    {
        return new QueueEntry(packet, delay);
    }

    public static QueueEntry fromAction(Consumer<ProtocolManager> action)
    {
        return fromAction(action, 0);
    }
    public static QueueEntry fromAction(Consumer<ProtocolManager> action, int delay)
    {
        return new QueueEntry(null, action, delay);
    }
}
