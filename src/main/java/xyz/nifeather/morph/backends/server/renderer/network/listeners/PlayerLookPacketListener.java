package xyz.nifeather.morph.backends.server.renderer.network.listeners;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;

/**
 * Listener used to fix the rotation issue for Phantom/Ender Dragon disguise
 */
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
            case PacketType.Play.Server.ENTITY_POSITION_SYNC ->
            {
                var wrapper = new WrapperPlayServerEntityPositionSync(event);
                var data = wrapper.getValues();
                var rec = this.getConvertedYawPitch(wrapper.getId(), data.getYaw(), data.getPitch());

                if (rec == null)
                    return;

                event.markForReEncode(true);
                data.setYaw(rec.yaw);
                data.setPitch(rec.pitch);
            }

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
        var sourcePlayer = getPlayerFrom(entityId);
        if (sourcePlayer == null)
            return null;

        var watcher = registry.getWatcher(sourcePlayer.getUniqueId());

        if (watcher == null)
            return null;

        return new YawPitchRec(
                watcher.readEntryOrDefault(CustomEntries.OVERLAYED_YAW, rawYaw),
                watcher.readEntryOrDefault(CustomEntries.OVERLAYED_PITCH, rawPitch)
        );
    }
}
