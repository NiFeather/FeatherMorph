package xiamomc.morph.backends.server.renderer.network;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.level.GameType;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.AbstractValues;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.registries.CustomEntries;
import xiamomc.morph.backends.server.renderer.utilties.ProtocolRegistryUtils;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.misc.DisguiseEquipment;
import xiamomc.morph.misc.MorphGameProfile;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.morph.utilities.EntityTypeUtils;
import xiamomc.morph.utilities.NmsUtils;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PacketFactory extends MorphPluginObject
{
    /**
     * 如果使用ProtocolLib自己的META系统<br>
     * 则可能会出现已发送的包又重新回归的问题<br>
     * 因此我们使用自己的方法，来标记某个包是否属于我们。
     */
    private final Cache<Integer, Object> cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.SECONDS).build();

    public void markPacketOurs(PacketContainer container)
    {
        cache.put(container.getHandle().hashCode(), container);
    }

    public boolean isPacketOurs(PacketContainer container)
    {
        return cache.getIfPresent(container.getHandle().hashCode()) != null;
    }

    public List<PacketContainer> buildSpawnPackets(DisplayParameters parameters)
    {
        var watcher = parameters.getWatcher();
        var player = watcher.getBindingPlayer();

        List<PacketContainer> packets = new ObjectArrayList<>();

        //logger.info("Build spawn packets, player is " + player.getName() + " :: parameters are " + parameters);

        var disguiseType = parameters.getWatcher().getEntityType();
        var nmsType = EntityTypeUtils.getNmsType(disguiseType);
        if (nmsType == null)
        {
            logger.error("No NMS Type for Bukkit Type '%s'".formatted(disguiseType));
            logger.error("Not build spawn packets!");

            //addSchedule(() -> registry.unregister(player));
            return packets;
        }

        var nmsPlayer = NmsRecord.ofPlayer(player);
        UUID spawnUUID = watcher.readEntryOrThrow(CustomEntries.SPAWN_UUID);
        if (spawnUUID.equals(Util.NIL_UUID))
            throw new IllegalStateException("A watcher with NIL UUID?!");

        //如果是玩家
        if (disguiseType == org.bukkit.entity.EntityType.PLAYER)
        {
            var gameProfile = watcher.readEntryOrThrow(CustomEntries.PROFILE);

            var packetTabRemove = new ClientboundPlayerInfoRemovePacket(List.of(spawnUUID));
            packets.add(PacketContainer.fromPacket(packetTabRemove));

            if (gameProfile.getName().isBlank())
                throw new IllegalArgumentException("GameProfile name is empty!");

            var packetPlayerInfo = new ClientboundPlayerInfoUpdatePacket(
                    EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED),
                    new ClientboundPlayerInfoUpdatePacket.Entry(
                            spawnUUID, gameProfile,
                            watcher.readEntryOrDefault(CustomEntries.PROFILE_LISTED, false),
                            114514, GameType.DEFAULT_MODE,
                            null, null
                    )
            );

            packets.add(PacketContainer.fromPacket(packetPlayerInfo));
        }

        var pitch = player.getPitch();
        var yaw = player.getYaw();

        if (disguiseType == EntityType.PHANTOM)
            pitch = -player.getPitch();

        if (disguiseType == EntityType.ENDER_DRAGON)
            yaw = 180 + yaw;

        //生成实体
        var packetAdd = new ClientboundAddEntityPacket(
                watcher.readEntryOrThrow(CustomEntries.SPAWN_ID), spawnUUID,
                player.getX(), player.getY(), player.getZ(),
                pitch, yaw,
                nmsType, 0,
                nmsPlayer.getDeltaMovement(),
                nmsPlayer.getYHeadRot()
        );

        var spawnPacket = PacketContainer.fromPacket(packetAdd);

        packets.add(spawnPacket);

        //生成装备和Meta
        var displayingFake = watcher.readEntryOrDefault(CustomEntries.DISPLAY_FAKE_EQUIPMENT, false);
        var equip = displayingFake
                ? watcher.readEntryOrDefault(CustomEntries.EQUIPMENT, new DisguiseEquipment())
                : player.getEquipment();

        var equipmentPacket = new ClientboundSetEquipmentPacket(player.getEntityId(),
                ProtocolEquipment.toPairs(equip));

        packets.add(PacketContainer.fromPacket(equipmentPacket));

        if (parameters.includeMetaPackets())
            packets.add(buildFullMetaPacket(player, parameters.getWatcher()));

        // 载具
        if (player.getVehicle() != null)
        {
            var nmsEntity = ((CraftEntity)player.getVehicle()).getHandle();
            packets.add(PacketContainer.fromPacket(new ClientboundSetPassengersPacket(nmsEntity)));
        }

        if (!player.getPassengers().isEmpty())
            packets.add(PacketContainer.fromPacket(new ClientboundSetPassengersPacket(nmsPlayer)));

        // 属性
        var bukkitEntityType = parameters.getWatcher().getEntityType();
        if (bukkitEntityType.isAlive())
        {
            //Attributes
            List<AttributeInstance> attributes = bukkitEntityType == EntityType.PLAYER
                    ? new ObjectArrayList<>(nmsPlayer.getAttributes().getSyncableAttributes())
                    : NmsUtils.getValidAttributes(bukkitEntityType, nmsPlayer.getAttributes());

            var attributePacket = new ClientboundUpdateAttributesPacket(player.getEntityId(), attributes);
            packets.add(PacketContainer.fromPacket(attributePacket));
        }

        for (PacketContainer packet : packets)
            markPacketOurs(packet);

        return packets;
    }

    /**
     * 重构服务器将要发送的Meta包
     * <br>
     * 直接修改Meta包会导致一些玄学问题，例如修改后的值在之后被发给了不该收到的人
     * @return 剔除后的包
     */
    public PacketContainer rebuildServerMetaPacket(AbstractValues av, SingleWatcher watcher, PacketContainer originalPacket)
    {
        if (originalPacket.getType() != PacketType.Play.Server.ENTITY_METADATA)
            throw new IllegalArgumentException("Original packet is not a valid metadata packet!");

        var newPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        newPacket.getIntegers().write(0, originalPacket.getIntegers().read(0));

        var values = av.getValues();

        //获取原Meta包中的数据
        var originalData = originalPacket.getDataValueCollectionModifier().read(0);

        List<WrappedDataValue> valuesToAdd = new ObjectArrayList<>();
        var blockedValues = watcher.getBlockedValues();

        for (WrappedDataValue w : originalData)
        {
            var index = w.getIndex();
            var rawValue = w.getRawValue();

            // 跳过被屏蔽的数据
            if (blockedValues.contains(index))
                continue;

            // 寻找与其匹配的SingleValue
            var disguiseValue = values.stream()
                    .filter(sv -> sv.index() == index && (rawValue == null || rawValue.getClass() == sv.defaultValue().getClass()))
                    .findFirst().orElse(null);

            // 如果没有找到，则代表此Index和伪装不兼容，跳过
            if (disguiseValue == null)
                continue;

            // 从Watcher获取要设定的数据值，如果没有，则从服务器的包里取
            var val = watcher.readOr(disguiseValue.index(), null);
            if (val == null) val = w.getRawValue();

            WrappedDataWatcher.Serializer serializer;

            try
            {
                serializer = ProtocolRegistryUtils.getSerializer(disguiseValue);
            }
            catch (Throwable t)
            {
                logger.warn("Error occurred while generating meta packet with id '%s': %s".formatted(disguiseValue.name(), t.getMessage()));
                continue;
            }

            valuesToAdd.add(new WrappedDataValue(disguiseValue.index(), serializer, val));
        }

        newPacket.getDataValueCollectionModifier().write(0, valuesToAdd);

        metaPacketIndex++;
        markPacketOurs(newPacket);

        return newPacket;
    }

    private int metaPacketIndex;

    public PacketContainer buildDiffMetaPacket(SingleWatcher watcher)
    {
        var metaPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        metaPacket.getIntegers().write(0, watcher.readEntryOrThrow(CustomEntries.SPAWN_ID));

        var modifier = metaPacket.getDataValueCollectionModifier();

        List<WrappedDataValue> wrappedDataValues = new ObjectArrayList<>();
        var valuesToSent = watcher.getDirty();
        watcher.clearDirty();

        valuesToSent.forEach((single, val) ->
        {
            WrappedDataWatcher.Serializer serializer;

            try
            {
                serializer = ProtocolRegistryUtils.getSerializer(single);
            }
            catch (Throwable t)
            {
                logger.warn("Error occurred while generating meta packet with id '%s': %s".formatted(single.name(), t.getMessage()));
                return;
            }

            var value = new WrappedDataValue(single.index(), serializer, val);
            wrappedDataValues.add(value);
        });

        modifier.write(0, wrappedDataValues);

        metaPacketIndex++;
        markPacketOurs(metaPacket);

        return metaPacket;
    }

    public PacketContainer buildFullMetaPacket(Player player, SingleWatcher watcher)
    {
        watcher.sync();

        var metaPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        metaPacket.getIntegers().write(0, player.getEntityId());

        var modifier = metaPacket.getDataValueCollectionModifier();

        List<WrappedDataValue> wrappedDataValues = new ObjectArrayList<>();

        var valuesToSent = watcher.getOverlayedRegistry();
        watcher.clearDirty();

        valuesToSent.forEach((index, val) ->
        {
            WrappedDataWatcher.Serializer serializer;
            var sv = watcher.getSingle(index);

            if (sv == null)
                throw new IllegalArgumentException("Not SingleValue found for index " + index);

            try
            {
                serializer = ProtocolRegistryUtils.getSerializer(sv);
            }
            catch (Throwable t)
            {
                logger.warn("Error occurred while generating meta packet with id '%s': %s".formatted(index, t.getMessage()));
                return;
            }

            var value = new WrappedDataValue(index, serializer, val);
            wrappedDataValues.add(value);
        });

        modifier.write(0, wrappedDataValues);

        metaPacketIndex++;
        markPacketOurs(metaPacket);

        return metaPacket;
    }

    public PacketContainer getEquipmentPacket(Player player, SingleWatcher watcher)
    {
        var shouldDisplayFakeEquip = watcher.readEntryOrDefault(CustomEntries.DISPLAY_FAKE_EQUIPMENT, false);
        EntityEquipment equipment = shouldDisplayFakeEquip
                    ? watcher.readEntryOrDefault(CustomEntries.EQUIPMENT, new DisguiseEquipment())
                    : player.getEquipment();

        var rawPacket = new ClientboundSetEquipmentPacket(player.getEntityId(),
                ProtocolEquipment.toPairs(equipment));

        var container = PacketContainer.fromPacket(rawPacket);
        markPacketOurs(container);

        return container;
    }
}
