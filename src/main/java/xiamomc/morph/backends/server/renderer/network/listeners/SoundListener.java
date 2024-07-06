package xiamomc.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.sound.StaticSound;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.registries.RenderRegistry;
import xiamomc.morph.utilities.EntityTypeUtils;
import xiamomc.pluginbase.Annotations.Resolved;

public class SoundListener extends ProtocolListener
{
    @Override
    public String getIdentifier()
    {
        return "sound";
    }

    public void onPacketSending(PacketSendEvent event)
    {
        var packetType = event.getPacketType();

        if (packetType != PacketType.Play.Server.NAMED_SOUND_EFFECT)
            return;

        var wrapper = new WrapperPlayServerSoundEffect(event);
        var playerToReceive = (Player)event.getPlayer();
        var playerLocation = playerToReceive.getLocation();

        var sourceX = wrapper.getEffectPosition().getX();
        var sourceY = wrapper.getEffectPosition().getY();
        var sourceZ = wrapper.getEffectPosition().getZ();

        // 寻找符合音源位置的伪装
        var watcher = registry.getWatchers().stream().filter(w ->
        {
            if (!w.isActive()) return false;

            var loc = w.getBindingPlayer().getLocation();
            if (!loc.getWorld().equals(playerLocation.getWorld())) return false;

            int locX = (int) (loc.x() * 8);
            int locY = (int) (loc.y() * 8);
            int locZ = (int) (loc.z() * 8);

            return sourceX == locX && sourceY == locY && sourceZ == locZ;
        }).findFirst().orElse(null);

        // 不要处理玩家伪装
        if (watcher == null || watcher.getEntityType() == EntityType.PLAYER) return;

        // 设置Sound
        var originalSound = wrapper.getSound();
        var location = originalSound.getSoundId();

        var path = location.getKey();

        // 如果以".hurt"结尾，那么尝试覆盖此音效
        if (path.endsWith(".hurt")
                || path.endsWith(".hurt_on_fire")
                || path.endsWith(".hurt_drown")
                || path.endsWith(".hurt_freeze")
                || path.endsWith(".hurt_sweet_berry_bush"))
        {
            var soundId = EntityTypeUtils.getDamageSound(watcher.getEntityType());
            if (soundId == null) return;

            ResourceLocation rL = new ResourceLocation(soundId);
            wrapper.setSound(new StaticSound(rL, wrapper.getSound().getRange()));
        }

    }

    @Resolved(shouldSolveImmediately = true)
    private RenderRegistry registry;
}
