package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import xyz.nifeather.morph.backends.server.renderer.network.PacketFactory;
import xyz.nifeather.morph.backends.server.renderer.network.ProtocolEquipment;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.NmsRecord;
import xyz.nifeather.morph.utilities.EntityTypeUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EntityWatcher extends SingleWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.BASE_ENTITY);
    }

    public EntityWatcher(Player bindingPlayer, EntityType entityType)
    {
        super(bindingPlayer, entityType);
    }

    protected byte getPlayerBitMask(Player player)
    {
        byte bitMask = 0x00;
        if (player.getFireTicks() > 0 || player.isVisualFire())
            bitMask |= (byte) 0x01;

        if (player.isSneaking())
            bitMask |= (byte) 0x02;

        if (player.isSprinting())
            bitMask |= (byte) 0x08;

        if (player.isSwimming())
            bitMask |= (byte) 0x10;

        if (player.isInvisible())
            bitMask |= (byte) 0x20;

        if (player.isGlowing())
            bitMask |= (byte) 0x40;

        if (NmsRecord.ofPlayer(player).isFallFlying())
            bitMask |= (byte) 0x80;

        return bitMask;
    }

    protected WrapperPlayServerEntityEquipment getEquipmentPacket()
    {
        var player = getBindingPlayer();
        var shouldDisplayFakeEquip = this.readEntryOrDefault(CustomEntries.DISPLAY_FAKE_EQUIPMENT, false);
        EntityEquipment equipment = shouldDisplayFakeEquip
                ? this.readEntryOrDefault(CustomEntries.EQUIPMENT, new DisguiseEquipment.EmptyDisguiseEquipment())
                : player.getEquipment();

        var packet = new WrapperPlayServerEntityEquipment(player.getEntityId(), ProtocolEquipment.toPEEquipmentList(equipment));

        PacketFactory.markEquipmentPacket(packet);

        return packet;
    }

    @Override
    public List<PacketWrapper<?>> buildSpawnPackets()
    {
        List<PacketWrapper<?>> packets = new ObjectArrayList<>();

        if (this.readEntryOrDefault(CustomEntries.VANISHED, false))
            return packets;

        var disguiseEntityType = this.getEntityType();

        var nmsSpawnType = EntityTypeUtils.getNmsType(disguiseEntityType);
        if (nmsSpawnType == null)
        {
            logger.error("No NMS Type for Bukkit Type '%s'".formatted(disguiseEntityType));
            logger.error("Not building spawn packets!");

            return packets;
        }

        var player = getBindingPlayer();
        var nmsPlayer = NmsRecord.ofPlayer(player);
        UUID spawnUUID = this.readEntryOrThrow(CustomEntries.SPAWN_UUID);
        if (spawnUUID.equals(Util.NIL_UUID))
            throw new IllegalStateException("A watcher with NIL UUID?!");

        var packetDestroy = new WrapperPlayServerDestroyEntities(this.readEntryOrThrow(CustomEntries.SPAWN_ID));
        packets.add(packetDestroy);

        //todo: Should we use a better way to get the yaw/pitch?
        //      I don't want to read yaw/pitch from player directly, so I used OVERLAYED_XXX to generate the value on call, so that other watchers can override the value
        var pitch = this.readEntryOrDefault(CustomEntries.OVERLAYED_PITCH, player.getPitch());
        var yaw = this.readEntryOrDefault(CustomEntries.OVERLAYED_YAW, player.getYaw());

        //生成实体
        var playerMotion = player.getVelocity();
        var spawnPacket = new WrapperPlayServerSpawnEntity(
                this.readEntryOrThrow(CustomEntries.SPAWN_ID), spawnUUID,
                SpigotConversionUtil.fromBukkitEntityType(disguiseEntityType),
                new Location(new Vector3d(player.getX(), player.getY(), player.getZ()), yaw, pitch),
                nmsPlayer.getYHeadRot(), 0,
                new Vector3d(playerMotion.getX(), playerMotion.getY(), playerMotion.getZ())
        );

        packets.add(spawnPacket);
        packets.add(getEquipmentPacket());
        packets.add(PacketFactory.buildFullMetaPacket(player, this));

        // 载具
        if (player.getVehicle() != null)
        {
            int[] passengers = player.getVehicle().getPassengers()
                    .stream()
                    .mapToInt(Entity::getEntityId)
                    .toArray();

            packets.add(new WrapperPlayServerSetPassengers(player.getVehicle().getEntityId(), passengers));
        }

        if (!player.getPassengers().isEmpty())
        {
            int[] passengers = player.getPassengers()
                    .stream()
                    .mapToInt(Entity::getEntityId)
                    .toArray();

            packets.add(new WrapperPlayServerSetPassengers(player.getEntityId(), passengers));
        }

        // 属性交由 LivingEntityWatcher 添加

        return packets;
    }

    @Override
    protected void doSync()
    {
        super.doSync();

        var player = getBindingPlayer();
        var values = ValueIndex.BASE_ENTITY;

        writeTemp(values.GENERAL, getPlayerBitMask(player));
        //write(values.SILENT, true);
        writeTemp(values.NO_GRAVITY, !player.hasGravity());
        writeTemp(values.POSE, SpigotConversionUtil.fromBukkitPose(player.getPose()));
        writeTemp(values.FROZEN_TICKS, player.getFreezeTicks());
    }

    @Override
    protected <X> void onEntryWrite(CustomEntry<X> entry, X oldVal, X newVal)
    {
        super.onEntryWrite(entry, oldVal, newVal);

        if (entry.equals(CustomEntries.DISGUISE_NAME) && this.getEntityType() != EntityType.PLAYER)
        {
            var str = newVal.toString();
            var component = str.isEmpty() ? null : Component.text(str);
            writePersistent(ValueIndex.BASE_ENTITY.CUSTOM_NAME, component == null ? Optional.empty() : Optional.of(component));
        }

        if (entry.equals(CustomEntries.VANISHED))
        {
            var packet = new WrapperPlayServerDestroyEntities(this.readEntryOrThrow(CustomEntries.SPAWN_ID));
            this.sendPacketToAffectedPlayers(packet);
        }
    }

    @Override
    public void mergeFromCompound(CompoundTag nbt)
    {
        super.mergeFromCompound(nbt);

        if (nbt.contains("CustomName"))
        {
            var name = nbt.getString("CustomName").orElseThrow();

            try
            {
                var component = JSONComponentSerializer.json().deserialize(name);

                writePersistent(ValueIndex.BASE_ENTITY.CUSTOM_NAME, Optional.of(component));
            }
            catch (Throwable t)
            {
                logger.error("Unable to parse CustomName '%s': %s".formatted(name, t.getMessage()));
            }
        }

        if (nbt.contains("CustomNameVisible"))
        {
            var visible = nbt.getBoolean("CustomNameVisible").orElseThrow();
            writePersistent(ValueIndex.BASE_ENTITY.CUSTOM_NAME_VISIBLE, visible);
        }
    }

    @Override
    public void writeToCompound(CompoundTag nbt)
    {
        super.writeToCompound(nbt);

        var customName = read(ValueIndex.BASE_ENTITY.CUSTOM_NAME);
        customName.ifPresent(c -> nbt.putString("CustomName", JSONComponentSerializer.json().serialize(c)));

        nbt.putBoolean("CustomNameVisible", read(ValueIndex.BASE_ENTITY.CUSTOM_NAME_VISIBLE));
    }
}
