package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;

import java.util.List;

public class PlayerWatcher extends AbstractPlayerWatcher
{
    public PlayerWatcher(Player bindingPlayer)
    {
        super(bindingPlayer);
    }

    @Override
    public void onDisguiseApply()
    {
        super.onDisguiseApply();

        var player = getBindingPlayer();
        this.buildPlayerInfoPackets().forEach(packet ->
        {
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        });
    }

    @Override
    protected void onDispose()
    {
        super.onDispose();

        var player = getBindingPlayer();
        var packet = new WrapperPlayServerPlayerInfoRemove(List.of(this.readEntryOrThrow(CustomEntries.SPAWN_UUID)));
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }
}
