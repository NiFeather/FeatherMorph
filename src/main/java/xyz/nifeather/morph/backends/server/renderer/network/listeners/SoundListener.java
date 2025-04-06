package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.sound.StaticSound;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntitySoundEffect;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;
import xyz.nifeather.morph.utilities.EntityTypeUtils;

public class SoundListener extends ProtocolListener
{
    @Override
    public String getIdentifier()
    {
        return "sound";
    }

    @Override
    public void onPacketSend(PacketSendEvent event)
    {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_SOUND_EFFECT)
            return;

        var wrapper = new WrapperPlayServerEntitySoundEffect(event);

        this.reEncodeSoundPacket(event, wrapper);
    }

    @Resolved(shouldSolveImmediately = true)
    private RenderRegistry registry;

    private void reEncodeSoundPacket(PacketSendEvent event, WrapperPlayServerEntitySoundEffect wrapper)
    {
        var player = this.getNmsPlayerFrom(wrapper.getEntityId());
        if (player == null)
        {
            return;
        }

        var theirWatcher = registry.getWatcher(player.getUUID());
        if (theirWatcher == null)
            return;

        event.markForReEncode(true);

        var sound = wrapper.getSound().getSoundId();
        var path = sound.toString();

        if (path.endsWith(".hurt")
                || path.endsWith(".hurt_on_fire")
                || path.endsWith(".hurt_drown")
                || path.endsWith(".hurt_freeze")
                || path.endsWith(".hurt_sweet_berry_bush"))
        {
            var soundId = EntityTypeUtils.getDamageSoundKey(theirWatcher.getEntityType());
            if (soundId == null) return;

            ResourceLocation rL = new ResourceLocation(soundId);

            wrapper.setSound(new StaticSound(rL, wrapper.getVolume()));
        }
    }
}
