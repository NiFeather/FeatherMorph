package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import xyz.nifeather.morph.backends.server.renderer.network.PacketFactory;
import xyz.nifeather.morph.backends.server.renderer.network.ProtocolEquipment;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.BindableVirtualEntity;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.ValueIndex;
import xyz.nifeather.morph.misc.BuildFailedException;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.utilities.EntityTypeUtils;
import xyz.nifeather.morph.utilities.FoliaThreadUtils;
import xyz.nifeather.morph.utilities.Uuids;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeoutException;

public class EntityWatcher extends BindableVirtualEntity<LivingEntity>
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.BASE_ENTITY);
    }

    public EntityWatcher(IBindTarget bindTarget, EntityType entityType)
    {
        super(bindTarget, entityType);
    }

    @Override
    public boolean isActive()
    {
        return bindTarget.isActive();
    }

    @Override
    public org.bukkit.Location location()
    {
        return bindTarget.location();
    }

    @Override
    public List<Player> getAffectedPlayers()
    {
        return bindTarget.viewingPlayers();
    }


    protected byte getPlayerBitMask()
    {
        return bindTarget.dataFlags();
    }

    protected WrapperPlayServerEntityEquipment getEquipmentPacket()
    {
        var shouldDisplayFakeEquip = this.readEntryOrDefault(CustomEntries.DISPLAY_FAKE_EQUIPMENT, false);

        DisguiseEquipment equipment = shouldDisplayFakeEquip
                ? this.readEntryOrDefault(CustomEntries.EQUIPMENT, DisguiseEquipment.empty())
                : DisguiseEquipment.copy(bindTarget.equipment());

        var packet = new WrapperPlayServerEntityEquipment(this.readEntryOrThrow(CustomEntries.SPAWN_ID), ProtocolEquipment.toPEEquipmentList(equipment));
        PacketFactory.markEquipmentPacket(packet);

        return packet;
    }

    public static final int PACKET_MARK = 10998;

    protected WrapperPlayServerEntityMetadata buildFullMetaPacket()
    {
        this.sync();

        List<EntityData<?>> wrappedDataValues = new ObjectArrayList<>();

        // Add our packet identifier!
        if (!this.readEntryOrDefault(CustomEntries.DONT_INCLUDE_PACKET_IDENTIFIER, false))
            wrappedDataValues.add(new EntityData<>(99, EntityDataTypes.STRING, PacketFactory.MARK_DONT_PROCESS));

        var valuesToSent = this.getOverlayedRegistry();
        this.clearDirty();

        valuesToSent.forEach((index, val) ->
        {
            var sv = this.getSingle(index);

            if (sv == null)
                throw new IllegalArgumentException("Not SingleValue found for index " + index);

            var wrapped =  new EntityData(index, sv.type(), val);
            wrappedDataValues.add(wrapped);
        });

        return new WrapperPlayServerEntityMetadata(this.readEntryOrThrow(CustomEntries.SPAWN_ID), wrappedDataValues);
    }

    protected List<PacketWrapper<?>> doBuildSpawnPackets() throws BuildFailedException
    {
        List<PacketWrapper<?>> packets = new ObjectArrayList<>();

        UUID spawnUUID = this.readEntryOrThrow(CustomEntries.SPAWN_UUID);
        if (spawnUUID.equals(Uuids.NIL_UUID))
            throw new IllegalStateException("A watcher with NIL UUID?!");

        var packetDestroy = new WrapperPlayServerDestroyEntities(this.readEntryOrThrow(CustomEntries.SPAWN_ID));
        packets.add(packetDestroy);

        var bukkitLocation = location();

        //todo: Should we use a better way to get the yaw/pitch?
        //      I don't want to read yaw/pitch from player directly, so I used OVERLAYED_XXX to generate the value on call, so that other watchers can override the value
        var pitch = this.readEntryOrDefault(CustomEntries.OVERLAYED_PITCH, bukkitLocation.getPitch());
        var yaw = this.readEntryOrDefault(CustomEntries.OVERLAYED_YAW, bukkitLocation.getYaw());

        //生成实体
        var disguiseEntityType = this.getEntityType();
        var motion = bindTarget.motion();
        var spawnPacket = new WrapperPlayServerSpawnEntity(
                this.readEntryOrThrow(CustomEntries.SPAWN_ID), spawnUUID,
                SpigotConversionUtil.fromBukkitEntityType(disguiseEntityType),
                new Location(new Vector3d(bukkitLocation.getX(), bukkitLocation.getY(), bukkitLocation.getZ()), yaw, pitch),
                bindTarget.yHeadRotation(), PACKET_MARK,
                new Vector3d(motion.getX(), motion.getY(), motion.getZ())
        );

        packets.add(spawnPacket);
        packets.add(buildFullMetaPacket());

        // 载具
        var vehicle = bindTarget.vehicle();
        var passengers = bindTarget.passengers();
        int rootVehicle = -1;

        if (!passengers.isEmpty())
            rootVehicle = this.readEntryOrThrow(CustomEntries.SPAWN_ID);

        if (vehicle.isPresent())
        {
            rootVehicle = vehicle.get();
            passengers.addFirst(vehicle.get());
        }

        if (rootVehicle > -1)
        {
            var array = passengers.stream().mapToInt(x -> x).toArray();
            packets.add(new WrapperPlayServerSetPassengers(rootVehicle, array));
        }

        // 属性交由 LivingEntityWatcher 添加

        packets.add(getEquipmentPacket());
        return packets;
    }

    @Override
    public List<PacketWrapper<?>> buildVirtualEntityDisposalPackets() throws BuildFailedException
    {
        var rmPacket = new WrapperPlayServerDestroyEntities(this.readEntryOrThrow(CustomEntries.SPAWN_ID));
        return List.of(rmPacket);
    }

    @Override
    public final List<PacketWrapper<?>> buildSpawnPackets() throws BuildFailedException
    {
        List<PacketWrapper<?>> result = new ObjectArrayList<>();

        if (this.readEntryOrDefault(CustomEntries.VANISHED, false))
            return result;

        var disguiseEntityType = this.getEntityType();

        var nmsSpawnType = EntityTypeUtils.getNmsType(disguiseEntityType);
        if (nmsSpawnType == null)
        {
            logger.error("No NMS Type for Bukkit Type '%s'".formatted(disguiseEntityType));
            logger.error("Not building spawn packets!");

            return result;
        }

        try
        {
            return bindTarget.runSynchronously(t ->
            {
                try
                {
                    return doBuildSpawnPackets();
                }
                catch (BuildFailedException e)
                {
                    throw new RuntimeException(e);
                }
            }, FoliaThreadUtils.DEFAULT_WAIT_TIMEOUT);
        }
        catch (TimeoutException e)
        {
            //仅仅是服务器太慢导致的等待超时，不要立马取消玩家的变形会话
            throw new BuildFailedException("Waiting too long for server thread of player %s to respond!".formatted(bindTarget.name()), e)
                    .critical(false);
        }
        catch (InterruptedException e)
        {
            throw new BuildFailedException("Task has been interrupted, why?", e);
        }
        catch (CancellationException e)
        {
            throw new BuildFailedException("Task cancelled, why?", e);
        }
        catch (Throwable t)
        {
            throw new BuildFailedException("Unhandled exception while building packets!", t);
        }
    }

    @Override
    protected void doSync()
    {
        super.doSync();

        var values = ValueIndex.BASE_ENTITY;

        writeTemp(values.GENERAL, getPlayerBitMask());
        //write(values.SILENT, true);
        writeTemp(values.NO_GRAVITY, !bindTarget.hasGravity());
        writeTemp(values.POSE, SpigotConversionUtil.fromBukkitPose(bindTarget.pose()));
        writeTemp(values.FROZEN_TICKS, bindTarget.freezeTicks());
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
    //@Deprecated(forRemoval = true)
    public void writeToCompound(CompoundTag nbt)
    {
        var customName = read(ValueIndex.BASE_ENTITY.CUSTOM_NAME);
        customName.ifPresent(c -> nbt.putString("CustomName", JSONComponentSerializer.json().serialize(c)));

        nbt.putBoolean("CustomNameVisible", read(ValueIndex.BASE_ENTITY.CUSTOM_NAME_VISIBLE));
    }
}
