package xyz.nifeather.morph.backends.server.renderer.network;

import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.AbstractValues;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.MorphGameProfile;
import xyz.nifeather.morph.misc.NmsRecord;
import xyz.nifeather.morph.utilities.EntityTypeUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PacketFactory extends MorphPluginObject
{
    /**
     * 如果使用ProtocolLib自己的META系统<br>
     * 则可能会出现已发送的包又重新回归的问题<br>
     * 因此我们使用自己的方法，来标记某个包是否属于我们。
     */
    private final Cache<Integer, Object> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(1, TimeUnit.SECONDS)
            .build();

    private static final UUID nilUUID = new UUID(0, 0);

    private record PacketRecord(long timeStamp)
    {
    }

    public List<PacketWrapper<?>> buildSpawnPackets(DisplayParameters parameters)
    {
        var watcher = parameters.getWatcher();
        var player = watcher.getBindingPlayer();

        List<PacketWrapper<?>> packets = new ObjectArrayList<>();

        if (watcher.readEntryOrDefault(CustomEntries.VANISHED, false))
            return packets;

        //logger.info("Build spawn packets, player is " + player.getName() + " :: parameters are " + parameters);

        var disguiseEntityType = watcher.getEntityType();
        var nmsType = EntityTypeUtils.getNmsType(disguiseEntityType);
        if (nmsType == null)
        {
            logger.error("No NMS Type for Bukkit Type '%s'".formatted(disguiseEntityType));
            logger.error("Not build spawn packets!");

            //addSchedule(() -> registry.unregister(player));
            return packets;
        }

        var nmsPlayer = NmsRecord.ofPlayer(player);
        UUID spawnUUID = watcher.readEntryOrThrow(CustomEntries.SPAWN_UUID);
        if (spawnUUID.equals(nilUUID))
            throw new IllegalStateException("A watcher with NIL Entity UUID?!");

        //如果是玩家
        if (disguiseEntityType == org.bukkit.entity.EntityType.PLAYER)
        {
            var gameProfile = watcher.readEntryOrThrow(CustomEntries.PROFILE);

            if (gameProfile.getName().isBlank())
                throw new IllegalArgumentException("GameProfile name is empty!");

            packets.addAll(this.buildPlayerInfoPackets(watcher));
        }

        var pitch = player.getPitch();
        var yaw = player.getYaw();

        if (disguiseEntityType == EntityType.PHANTOM)
            pitch = -player.getPitch();

        if (disguiseEntityType == EntityType.ENDER_DRAGON)
            yaw = 180 + yaw;

        int spawnID = watcher.readEntryOrThrow(CustomEntries.SPAWN_ID);

        //生成实体
        var playerVelocity = player.getVelocity();
        var packetSpawn = new WrapperPlayServerSpawnEntity(
                spawnID, Optional.of(spawnUUID),
                SpigotConversionUtil.fromBukkitEntityType(disguiseEntityType),
                new Vector3d(player.getX(), player.getY(), player.getZ()),
                pitch, yaw,
                nmsPlayer.getYHeadRot(), 0,
                Optional.of(new Vector3d(playerVelocity.getX(), playerVelocity.getY(), playerVelocity.getZ()))
        );

        packets.add(packetSpawn);

        //生成装备和Meta
        packets.add(this.getEquipmentPacket(player, watcher));
        packets.add(buildFullMetaPacket(watcher));

        // 载具
        if (player.getVehicle() != null && !player.getVehicle().getPassengers().isEmpty())
        {
            var vehicle = player.getVehicle();

            var passengers = player.getPassengers();

            var passengerIDs = new int[passengers.size()];
            int index = 0;

            for (var e : passengers)
            {
                passengerIDs[index] = e.getEntityId();
                index++;
            }

            packets.add(new WrapperPlayServerSetPassengers(vehicle.getEntityId(), passengerIDs));
        }

        if (!player.getPassengers().isEmpty())
        {
            var passengers = player.getPassengers();

            var passengerIDs = new int[passengers.size()];
            int index = 0;

            for (var e : passengers)
            {
                passengerIDs[index] = e.getEntityId();
                index++;
            }

            packets.add(new WrapperPlayServerSetPassengers(spawnID, passengerIDs));
        }

        // 属性
        if (disguiseEntityType.isAlive())
        {
            List<WrapperPlayServerUpdateAttributes.Property> propertyList = new ObjectArrayList<>();

            List<Attribute> syncableAttributes = List.of(
                    Attribute.SCALE
            );

            for (Attribute attribute : syncableAttributes)
            {
                var packetAttribute = Attributes.getByName(attribute.key().asString());
                if (packetAttribute == null)
                {
                    logger.warn("Local attribute '%s' has no packet version!".formatted(attribute.key().asString()));
                    continue;
                }

                var instance = player.getAttribute(attribute);
                if (instance == null)
                {
                    logger.warn("Local attribute '%s' has no instance!".formatted(attribute.key().asString()));
                    continue;
                }

                List<WrapperPlayServerUpdateAttributes.PropertyModifier> modifiers = new ObjectArrayList<>();
                for (AttributeModifier modifier : instance.getModifiers())
                {
                    var packetModifier = new WrapperPlayServerUpdateAttributes.PropertyModifier(
                            new ResourceLocation(modifier.key().namespace(), modifier.key().value()),
                            modifier.getAmount(),
                            WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.values()[modifier.getOperation().ordinal()]
                    );

                    modifiers.add(packetModifier);
                }

                propertyList.add(new WrapperPlayServerUpdateAttributes.Property(packetAttribute, instance.getValue(), modifiers));
            }

            var attributePacket = new WrapperPlayServerUpdateAttributes(player.getEntityId(), propertyList);
            packets.add(attributePacket);
        }

        // todo: Implement this
        //for (PacketContainer packet : packets)
        //    markPacketOurs(packet);

        return packets;
    }

    public List<PacketWrapper<?>> buildPlayerInfoPackets(SingleWatcher watcher)
    {
        var spawnUUID = watcher.readEntryOrThrow(CustomEntries.SPAWN_UUID);
        var infoRemove = new WrapperPlayServerPlayerInfoRemove(spawnUUID);

        var infoUpdate = new WrapperPlayServerPlayerInfoUpdate(
                EnumSet.of(
                        WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
                        WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED
                ),
                new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                        MorphGameProfile.toPacketEventsUserProfile(watcher.readEntryOrThrow(CustomEntries.PROFILE)),
                        false,
                        114514,
                        GameMode.SURVIVAL, Component.text("???"), null
                )
        );

        return List.of(infoRemove, infoUpdate);
    }

    /**
     * 重构服务器将要发送的Meta包
     * <br>
     * 直接修改Meta包会导致一些玄学问题，例如修改后的值在之后被发给了不该收到的人
     * @return 剔除后的包
     */
    public void rebuildServerMetaList(AbstractValues av, SingleWatcher watcher, WrapperPlayServerEntityMetadata wrapper)
    {
        var values = av.getValues();

        //获取原Meta包中的数据
        var originalData = wrapper.getEntityMetadata();

        List<EntityData> valuesToAdd = new ObjectArrayList<>();
        var blockedValues = watcher.getBlockedValues();

        for (EntityData data : originalData)
        {
            var index = data.getIndex();

            // 跳过被屏蔽的数据
            if (blockedValues.contains(index))
                continue;

            // 寻找与其匹配的SingleValue
            var singleValue = values.stream()
                    .filter(sv -> sv.index() == index && data.getType().equals(sv.type()))
                    .findFirst().orElse(null);

            // 如果没有找到，则代表此Index和伪装不兼容，跳过
            if (singleValue == null)
                continue;

            // 如果 Watcher 中有覆盖的有对应的值，则重新包装，否则原样返回
            var val = watcher.readOr(singleValue.index(), null);

            if (val != null)
            {
                var replaceData = new EntityData(data.getIndex(), data.getType(), val);
                valuesToAdd.add(replaceData);
            }
            else
            {
                valuesToAdd.add(data);
            }
        }

        wrapper.setEntityMetadata(valuesToAdd);
    }

    public WrapperPlayServerEntityMetadata buildDiffMetaPacket(SingleWatcher watcher)
    {
        List<EntityData> wrappedDataValues = new ObjectArrayList<>();
        var valuesToSent = watcher.getDirty();
        watcher.clearDirty();

        valuesToSent.forEach((single, val) ->
        {
            var wrapped =  new EntityData(single.index(), single.type(), val);
            wrappedDataValues.add(wrapped);
        });

        return new WrapperPlayServerEntityMetadata(watcher.readEntryOrThrow(CustomEntries.SPAWN_ID), wrappedDataValues);
    }

    public WrapperPlayServerEntityMetadata buildFullMetaPacket(SingleWatcher watcher)
    {
        watcher.sync();

        List<EntityData> wrappedDataValues = new ObjectArrayList<>();

        var valuesToSent = watcher.getOverlayedRegistry();
        watcher.clearDirty();

        valuesToSent.forEach((index, val) ->
        {
            var sv = watcher.getSingle(index);

            if (sv == null)
                throw new IllegalArgumentException("Not SingleValue found for index " + index);

            var wrapped =  new EntityData(index, sv.type(), val);
            wrappedDataValues.add(wrapped);
        });

        return new WrapperPlayServerEntityMetadata(watcher.readEntryOrThrow(CustomEntries.SPAWN_ID), wrappedDataValues);
    }

    public List<Equipment> getPacketeventsEquipments(Player player, SingleWatcher watcher)
    {
        var shouldDisplayFakeEquip = watcher.readEntryOrDefault(CustomEntries.DISPLAY_FAKE_EQUIPMENT, false);
        EntityEquipment equipment = shouldDisplayFakeEquip
                ? watcher.readEntryOrDefault(CustomEntries.EQUIPMENT, new DisguiseEquipment())
                : player.getEquipment();

        return ProtocolEquipment.toPEEquipmentList(equipment);
    }

    public WrapperPlayServerEntityEquipment getEquipmentPacket(Player player, SingleWatcher watcher)
    {
        var list = getPacketeventsEquipments(player, watcher);

        return new WrapperPlayServerEntityEquipment(player.getEntityId(), list);
    }
}
