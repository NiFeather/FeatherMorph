package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;

public class PlayerLookPacketListener extends ProtocolListener
{
    @Override
    public String getIdentifier()
    {
        return "look_move_listener";
    }

    @Override
    public void onPacketSend(PacketSendEvent event)
    {
        switch (event.getPacketType())
        {
            case PacketType.Play.Server.ENTITY_HEAD_LOOK ->
            {
                var wrapper = new WrapperPlayServerEntityHeadLook(event);
                var rec = this.getConvertedYawPitch(wrapper.getEntityId(), wrapper.getHeadYaw(), 0f);

                if (rec == null)
                    return;

                event.markForReEncode(true);
                wrapper.setHeadYaw(rec.yaw());
            }

            case PacketType.Play.Server.ENTITY_ROTATION ->
            {
                var wrapper = new WrapperPlayServerEntityRotation(event);
                var rec = this.getConvertedYawPitch(wrapper.getEntityId(), wrapper.getYaw(), wrapper.getPitch());

                if (rec == null) return;

                event.markForReEncode(true);
                wrapper.setYaw(rec.yaw());
                wrapper.setPitch(rec.pitch());
            }

            case PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION ->
            {
                var wrapper = new WrapperPlayServerEntityRelativeMoveAndRotation(event);
                var rec = this.getConvertedYawPitch(wrapper.getEntityId(), wrapper.getYaw(), wrapper.getPitch());

                if (rec == null) return;

                event.markForReEncode(true);
                wrapper.setYaw(rec.yaw());
                wrapper.setPitch(rec.pitch());
            }

            case PacketType.Play.Server.ENTITY_TELEPORT ->
            {
                var wrapper = new WrapperPlayServerEntityTeleport(event);
                var rec = this.getConvertedYawPitch(wrapper.getEntityId(), wrapper.getYaw(), wrapper.getPitch());

                if (rec == null) return;

                event.markForReEncode(true);
                wrapper.setYaw(rec.yaw());
                wrapper.setPitch(rec.pitch());
            }

            default -> {}
        }
    }

    @Resolved(shouldSolveImmediately = true)
    private RenderRegistry registry;

    private record YawPitchRec(float yaw, float pitch)
    {
    }

    @Nullable
    private YawPitchRec getConvertedYawPitch(int entityId, float rawYaw, float rawPitch)
    {
        //获取此包的来源实体
        var sourceNmsEntity = getNmsPlayerFrom(entityId);
        if (sourceNmsEntity == null)
            return null;

        if (!(sourceNmsEntity.getBukkitEntity() instanceof Player sourcePlayer))
            return null;

        var watcher = registry.getWatcher(sourcePlayer.getUniqueId());

        if (watcher == null)
            return null;

        var isDragon = watcher.getEntityType() == EntityType.ENDER_DRAGON;
        var isPhantom = watcher.getEntityType() == EntityType.PHANTOM;

        if (!isDragon && !isPhantom)
            return null;

        float yaw, pitch;

        yaw = isDragon ? (rawYaw + 180f) : rawYaw;
        pitch = isPhantom ? -rawPitch : rawPitch;

<<<<<<< HEAD
        var container = event.getPacket();
        container.getBytes().write(0, Mth.packDegrees(yaw));
        container.getBytes().write(1, Mth.packDegrees(pitch));
    }

    private void onHeadRotation(ClientboundRotateHeadPacket packet, PacketEvent event)
    {
        //获取此包的来源实体
        var sourceNmsEntity = this.getNmsPlayerEntityFromUnreadablePacket(packet);
        if (sourceNmsEntity == null) return;

        if (!(sourceNmsEntity.getBukkitEntity() instanceof Player sourcePlayer)) return;

        var watcher = registry.getWatcher(sourcePlayer.getUniqueId());

        if (watcher == null || watcher.getEntityType() != EntityType.ENDER_DRAGON)
            return;

        var newHeadYaw = packet.getYHeadRot() + 180f;

        var newPacket = new ClientboundRotateHeadPacket(sourceNmsEntity, Mth.packDegrees(newHeadYaw));
        var finalPacket = PacketContainer.fromPacket(newPacket);

        event.setPacket(finalPacket);
    }

    private void onLookPacket(ClientboundMoveEntityPacket packet, PacketEvent event)
    {
        //获取此包的来源实体
        var sourceNmsEntity = this.getNmsPlayerEntityFromUnreadablePacket(packet);

        if (sourceNmsEntity == null) return;

        if (!(sourceNmsEntity.getBukkitEntity() instanceof Player sourcePlayer)) return;

        var watcher = registry.getWatcher(sourcePlayer.getUniqueId());

        if (watcher == null)
            return;

        var isDragon = watcher.getEntityType() == EntityType.ENDER_DRAGON;
        var isPhantom = watcher.getEntityType() == EntityType.PHANTOM;

        if (!isDragon && !isPhantom)
            return;

        float yaw = packet.getYRot();
        float pitch = packet.getXRot();

        yaw = isDragon ? (yaw + 180f) : yaw;
        pitch = isPhantom ? -pitch : pitch;

        ClientboundMoveEntityPacket newPacket;

        var packetType = event.getPacketType();

        if (packetType == PacketType.Play.Server.ENTITY_LOOK)
        {
            newPacket = new ClientboundMoveEntityPacket.Rot(
                    sourcePlayer.getEntityId(),
                    Mth.packDegrees(yaw), Mth.packDegrees(pitch),
                    packet.isOnGround()
            );
        }
        else if (packetType == PacketType.Play.Server.REL_ENTITY_MOVE_LOOK)
        {
            newPacket = new ClientboundMoveEntityPacket.PosRot(
                    sourcePlayer.getEntityId(),
                    packet.getXa(), packet.getYa(), packet.getZa(),
                    Mth.packDegrees(yaw), Mth.packDegrees(pitch),
                    packet.isOnGround()
            );
        }
        else
        {
            logger.error("Unknown ClientboundMoveEntityPacket: " + packetType);
            return;
        }

        var finalPacket = PacketContainer.fromPacket(newPacket);
        event.setPacket(finalPacket);
    }

    @Override
    public void onPacketReceiving(PacketEvent packetEvent)
    {
    }

    private final ListeningWhitelist listeningWhitelist = ListeningWhitelist.newBuilder()
            .types(PacketType.Play.Server.ENTITY_LOOK,
                    PacketType.Play.Server.ENTITY_HEAD_ROTATION,
                    PacketType.Play.Server.REL_ENTITY_MOVE_LOOK,
                    PacketType.Play.Server.ENTITY_TELEPORT)
            .build();

    @Override
    public ListeningWhitelist getSendingWhitelist()
    {
        return listeningWhitelist;
    }

    @Override
    public ListeningWhitelist getReceivingWhitelist()
    {
        return ListeningWhitelist.EMPTY_WHITELIST;
=======
        return new YawPitchRec(yaw, pitch);
>>>>>>> 1.21.4-packetevents
    }
}
