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

<<<<<<< HEAD
        // 不要处理玩家伪装
        if (watcher == null || watcher.getEntityType() == EntityType.PLAYER) return null;

        // 设置Sound
        var holder = handle.getSound();
        var unwrapOptional = holder.unwrap();

        var right = unwrapOptional.right();
        var left = unwrapOptional.left();

        // 获取正要播放的音效
        AtomicReference<ResourceLocation> resourceLocationRef = new AtomicReference<>(null);
        left.ifPresent(rK -> resourceLocationRef.set(rK.location()));
        right.ifPresent(se -> resourceLocationRef.set(se.location()));

        // 查无此声
        if (resourceLocationRef.get() == null)
            return null;

        SoundEvent sound = null;
        var location = resourceLocationRef.get();

        var path = location.getPath();

        // 如果以".hurt"结尾，那么尝试覆盖此音效
=======
>>>>>>> 1.21.4-packetevents
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
