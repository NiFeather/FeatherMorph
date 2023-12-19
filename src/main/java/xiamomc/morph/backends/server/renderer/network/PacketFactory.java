package xiamomc.morph.backends.server.renderer.network;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.level.GameType;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.utilties.ProtocolRegistryUtils;
import xiamomc.morph.misc.DisguiseEquipment;
import xiamomc.morph.misc.MorphGameProfile;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.morph.utilities.EntityTypeUtils;

import java.util.*;

public class PacketFactory extends MorphPluginObject
{
    public static final String MORPH_PACKET_METAKEY = "fm";

    public List<PacketContainer> buildSpawnPackets(Player player, DisplayParameters parameters)
    {
        List<PacketContainer> packets = new ObjectArrayList<>();

        //logger.info("Build spawn packets, player is " + player.getName() + " :: parameters are " + parameters);

        var disguiseType = parameters.getEntityType();
        var nmsType = EntityTypeUtils.getNmsType(disguiseType);
        if (nmsType == null)
        {
            logger.error("No NMS Type for Bukkit Type '%s'".formatted(disguiseType));
            logger.error("Not build spawn packets!");

            //addSchedule(() -> registry.unregister(player));
            return packets;
        }

        var nmsPlayer = NmsRecord.ofPlayer(player);
        UUID spawnUUID = player.getUniqueId();

        //如果是玩家
        if (disguiseType == org.bukkit.entity.EntityType.PLAYER)
        {
            //logger.info("Building player info packet!");

            var parametersProfile = parameters.getProfile();
            Objects.requireNonNull(parametersProfile, "Null game profile!");
            var gameProfile = new MorphGameProfile(parametersProfile);

            if (!parameters.dontRandomProfileUUID())
            {
                //todo: Get random UUID from world to prevent duplicate UUID
                //玩家在客户端的UUID会根据其GameProfile中的UUID设定，我们需要避免伪装的UUID和某一玩家自己的UUID冲突
                gameProfile.setUUID(UUID.randomUUID());
            }

            //Minecraft需要在生成玩家实体前先发送PlayerInfoUpdate消息
            var uuid = gameProfile.getId();
            var packetPlayerInfo = new ClientboundPlayerInfoUpdatePacket(
                    EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER),
                    new ClientboundPlayerInfoUpdatePacket.Entry(
                            uuid, gameProfile, false, 114514, GameType.DEFAULT_MODE,
                            Component.literal(":>"), null
                    )
            );

            spawnUUID = uuid;
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
                player.getEntityId(), spawnUUID,
                player.getX(), player.getY(), player.getZ(),
                pitch, yaw,
                nmsType, 0,
                nmsPlayer.getDeltaMovement(),
                nmsPlayer.getYHeadRot()
        );

        var spawnPacket = PacketContainer.fromPacket(packetAdd);

        packets.add(spawnPacket);

        //生成装备和Meta
        var equipmentPacket = new ClientboundSetEquipmentPacket(player.getEntityId(),
                ProtocolEquipment.toPairs(player.getEquipment()));
        packets.add(PacketContainer.fromPacket(equipmentPacket));

        if (parameters.includeMeta())
        {
            var watcher = parameters.getWatcher();
            packets.add(buildFullMetaPacket(player, watcher));
        }

        if (player.getVehicle() != null)
        {
            var nmsEntity = ((CraftEntity)player.getVehicle()).getHandle();
            packets.add(PacketContainer.fromPacket(new ClientboundSetPassengersPacket(nmsEntity)));
        }

        if (!player.getPassengers().isEmpty())
            packets.add(PacketContainer.fromPacket(new ClientboundSetPassengersPacket(NmsRecord.ofPlayer(player))));

        for (PacketContainer packet : packets)
            packet.setMeta(MORPH_PACKET_METAKEY, true);

        return packets;
    }

    public PacketContainer buildDiffMetaPacket(Player player, SingleWatcher watcher)
    {
        var metaPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        metaPacket.getIntegers().write(0, player.getEntityId());

        var modifier = metaPacket.getDataValueCollectionModifier();

        List<WrappedDataValue> wrappedDataValues = new ObjectArrayList<>();
        Map<SingleValue<?>, Object> valuesToSent = watcher.getDirty();

        valuesToSent.forEach((single, v) ->
        {
            WrappedDataWatcher.Serializer serializer;

            try
            {
                serializer = ProtocolRegistryUtils.getSerializer(single.defaultValue());
            }
            catch (Throwable t)
            {
                logger.warn("Error occurred while generating meta packet with id '%s': %s".formatted(single.index(), t.getMessage()));
                return;
            }

            var value = new WrappedDataValue(single.index(), serializer, v);
            wrappedDataValues.add(value);
        });

        modifier.write(0, wrappedDataValues);
        metaPacket.setMeta(MORPH_PACKET_METAKEY, true);

        return metaPacket;
    }

    public PacketContainer buildFullMetaPacket(Player player, SingleWatcher watcher)
    {
        watcher.sync();

        var metaPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        metaPacket.getIntegers().write(0, player.getEntityId());

        var modifier = metaPacket.getDataValueCollectionModifier();

        List<WrappedDataValue> wrappedDataValues = new ObjectArrayList<>();

        Map<SingleValue<?>, Object> valuesToSent = new Object2ObjectOpenHashMap<>();
        valuesToSent.putAll(watcher.getDirty());
        watcher.getRegistry().forEach((single, v) ->
        {
            if (single.defaultValue().equals(v)) return;
            valuesToSent.put(single, v);
        });

        valuesToSent.forEach((single, v) ->
        {
            WrappedDataWatcher.Serializer serializer;

            try
            {
                serializer = ProtocolRegistryUtils.getSerializer(single.defaultValue());
            }
            catch (Throwable t)
            {
                logger.warn("Error occurred while generating meta packet with id '%s': %s".formatted(single.index(), t.getMessage()));
                return;
            }

            var value = new WrappedDataValue(single.index(), serializer, v);
            wrappedDataValues.add(value);
        });

        modifier.write(0, wrappedDataValues);
        metaPacket.setMeta(MORPH_PACKET_METAKEY, true);

        return metaPacket;
    }

    public PacketContainer getEquipmentPacket(Player player, SingleWatcher watcher)
    {
        var shouldDisplayFakeEquip = watcher.getOrDefault(EntryIndex.DISPLAY_FAKE_EQUIPMENT, false);
        EntityEquipment equipment = shouldDisplayFakeEquip
                    ? watcher.getOrDefault(EntryIndex.EQUIPMENT, new DisguiseEquipment())
                    : player.getEquipment();

        var rawPacket = new ClientboundSetEquipmentPacket(player.getEntityId(),
                ProtocolEquipment.toPairs(equipment));

        var container = PacketContainer.fromPacket(rawPacket);
        container.setMeta(MORPH_PACKET_METAKEY, true);

        return container;
    }
}
