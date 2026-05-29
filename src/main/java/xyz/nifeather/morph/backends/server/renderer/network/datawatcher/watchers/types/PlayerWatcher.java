package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.misc.BuildFailedException;

import java.util.Collection;
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
    protected Collection<? extends PacketWrapper<?>> preSpawnPackets()
            throws BuildFailedException
    {
        var gameProfile = this.readEntryOrThrow(CustomEntries.PROFILE);

        if (gameProfile.name().isBlank())
            throw new BuildFailedException("GameProfile name is empty!");

        return buildPlayerInfoPackets();
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
