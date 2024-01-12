package xiamomc.morph.backends.server.renderer.network.queue;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.Player;
import xiamomc.morph.MorphPluginObject;
import xiamomc.pluginbase.Annotations.Initializer;

import java.util.List;
import java.util.Map;

public class PacketQueue extends MorphPluginObject
{
    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    private final Map<Player, List<QueueEntry>> playerPackets = new Object2ObjectOpenHashMap<>();

    public void clearQueue(Player bindingPlayer)
    {
        playerPackets.remove(bindingPlayer);
    }

    public void pushQueue(Player bindingPlayer, List<QueueEntry> queue)
    {
        var targetList = playerPackets.getOrDefault(bindingPlayer, null);
        if (targetList == null)
            playerPackets.put(bindingPlayer, targetList = new ObjectArrayList<>());

        List<QueueEntry> finalTargetList = targetList;

        var currentTick = plugin.getCurrentTick();
        queue.forEach(qs ->
        {
            if (qs.delay <= 0)
            {
                applyPacket(bindingPlayer, qs);
            }
            else
            {
                qs.targetTick = currentTick + qs.delay;
                finalTargetList.add(qs);
            }
        });
    }

    @Initializer
    private void load()
    {
        this.addSchedule(this::update);
    }

    private void update()
    {
        this.addSchedule(this::update);

        if (playerPackets.isEmpty())
            return;

        var currentTick = plugin.getCurrentTick();
        var packetsCopy = new Object2ObjectOpenHashMap<>(playerPackets);
        packetsCopy.forEach((player, queue) ->
        {
            if (!player.isOnline())
                playerPackets.remove(player);

            var queueCopy = new ObjectArrayList<>(queue);
            queueCopy.forEach(qs ->
            {
                if (qs.targetTick >= currentTick)
                {
                    this.applyPacket(player, qs);
                    queue.remove(qs);
                }
            });
        });
    }

    private void applyPacket(Player bindingPlayer, QueueEntry single)
    {
        if (single.packet != null)
            protocolManager.sendServerPacket(bindingPlayer, single.packet);

        if (single.action != null)
        {
            try
            {
                single.action.accept(protocolManager);
            }
            catch (Throwable t)
            {
                logger.error("Error occurred while performing action from queue: " + t.getMessage());
                t.printStackTrace();
            }
        }
    }
}
