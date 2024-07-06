package xiamomc.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.backends.server.renderer.network.registries.RenderRegistry;
import xiamomc.pluginbase.Annotations.Resolved;

public class PlayerLookPacketListener extends ProtocolListener
{
    @Override
    public String getIdentifier()
    {
        return "look_move_listener";
    }

    public void onPacketSending(PacketSendEvent event)
    {
        var packetType = event.getPacketType();

        //不要处理来自我们自己的包
        //if (packet.getMeta(PacketFactory.MORPH_PACKET_METAKEY).isPresent())
        //{
        //    return;
        //}

        switch (packetType)
        {
            case PacketType.Play.Server.ENTITY_HEAD_LOOK ->
            {
                var wrapper = new WrapperPlayServerEntityHeadLook(event);

                this.onHeadLook(wrapper, event);
            }

            case PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION ->
            {
                var wrapper = new WrapperPlayServerEntityRelativeMoveAndRotation(event);

                var yawPitch = this.getYawPitch(event, wrapper.getEntityId(), wrapper.getYaw(), wrapper.getPitch());
                if (yawPitch == null) return;

                wrapper.setYaw(yawPitch.yaw);
                wrapper.setPitch(yawPitch.pitch);
            }

            case PacketType.Play.Server.ENTITY_ROTATION ->
            {
                var wrapper = new WrapperPlayServerEntityRotation(event);

                var yawPitch = this.getYawPitch(event, wrapper.getEntityId(), wrapper.getYaw(), wrapper.getPitch());
                if (yawPitch == null) return;

                wrapper.setYaw(yawPitch.yaw);
                wrapper.setPitch(yawPitch.pitch);
            }

            case PacketType.Play.Server.ENTITY_TELEPORT ->
            {
                var wrapper = new WrapperPlayServerEntityTeleport(event);

                var yawPitch = this.getYawPitch(event, wrapper.getEntityId(), wrapper.getYaw(), wrapper.getPitch());
                if (yawPitch == null) return;

                wrapper.setYaw(yawPitch.yaw);
                wrapper.setPitch(yawPitch.pitch);
            }

            default ->
            {
            }
        }
    }

    @Resolved(shouldSolveImmediately = true)
    private RenderRegistry registry;

    @Nullable
    private YawPitch getYawPitch(PacketSendEvent event, int entityId, float originalYaw, float originalPitch)
    {
        //获取此包的来源实体
        var sourceNmsEntity = getNmsEntityFrom(event, entityId);
        if (sourceNmsEntity == null)
            return null;

        if (!(sourceNmsEntity.getBukkitEntity() instanceof Player sourcePlayer)) return null;

        var watcher = registry.getWatcher(sourcePlayer.getUniqueId());

        if (watcher == null)
            return null;

        var isDragon = watcher.getEntityType() == EntityType.ENDER_DRAGON;
        var isPhantom = watcher.getEntityType() == EntityType.PHANTOM;

        if (!isDragon && !isPhantom)
            return null;

        var yaw = originalYaw;
        var pitch = originalPitch;

        var playerYaw = isDragon ? (sourcePlayer.getYaw() + 180f) : sourcePlayer.getYaw();
        var finalYaw = (playerYaw / 360f) * 256f;
        yaw = (byte)finalYaw;

        var playerPitch = isPhantom ? -sourcePlayer.getPitch() : sourcePlayer.getPitch();

        var finalPitch = (playerPitch / 360f) * 256f;
        pitch = (byte)finalPitch;

        return new YawPitch(yaw, pitch);
    }

    private record YawPitch(float yaw, float pitch)
    {
    }

    private void onHeadLook(WrapperPlayServerEntityHeadLook packet, PacketSendEvent event)
    {
        //获取此包的来源实体
        var sourceNmsEntity = getNmsEntityFrom(event, packet.getEntityId());
        if (sourceNmsEntity == null)
        {
            if (isDebugEnabled())
            {
                logger.warn("A packet from a player that doesn't exist in its world?!");
                logger.warn("Packet: " + event.getPacketType());
            }

            return;
        }

        if (!(sourceNmsEntity.getBukkitEntity() instanceof Player sourcePlayer)) return;

        var watcher = registry.getWatcher(sourcePlayer.getUniqueId());

        if (watcher == null || watcher.getEntityType() != EntityType.ENDER_DRAGON)
            return;

        var newHeadYaw = (byte)(((sourcePlayer.getYaw() + 180f) / 360f) * 256f);

        packet.setHeadYaw(newHeadYaw);
    }

}
