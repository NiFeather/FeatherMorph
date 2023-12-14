package xiamomc.morph.backends.server.renderer.network;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.level.GameType;
import org.bukkit.entity.Player;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.utilties.ProtocolRegistryUtils;
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

        var playerType = parameters.getEntityType();
        var nmsType = EntityTypeUtils.getNmsType(playerType);
        if (nmsType == null)
        {
            logger.error("No NMS Type for Bukkit Type '%s'".formatted(playerType));
            logger.error("Not build spawn packets!");

            //addSchedule(() -> registry.unregister(player));
            return packets;
        }

        var nmsPlayer = NmsRecord.ofPlayer(player);
        UUID spawnUUID = player.getUniqueId();

        //如果是玩家
        if (playerType == org.bukkit.entity.EntityType.PLAYER)
        {
            //logger.info("Building player info packet!");

            var parametersProfile = parameters.getProfile();
            Objects.requireNonNull(parametersProfile, "Null game profile!");
            var gameProfile = new MorphGameProfile(parametersProfile);

            //todo: Get random UUID from world
            //玩家在客户端的UUID会根据其GameProfile中的UUID设定，我们需要避免伪装的UUID和某一玩家自己的UUID冲突
            gameProfile.setUUID(UUID.randomUUID());

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

        //生成实体
        var packetAdd = new ClientboundAddEntityPacket(
                player.getEntityId(), spawnUUID,
                player.getX(), player.getY(), player.getZ(),
                player.getPitch(), player.getYaw(),
                nmsType, 0,
                nmsPlayer.getDeltaMovement(),
                nmsPlayer.getYHeadRot()
        );

        var spawnPacket = PacketContainer.fromPacket(packetAdd);

        spawnPacket.setMeta(MORPH_PACKET_METAKEY, true);
        packets.add(spawnPacket);

        //生成装备和Meta
        var equipmentPacket = new ClientboundSetEquipmentPacket(player.getEntityId(),
                ProtocolEquipment.toPairs(player.getEquipment()));
        packets.add(PacketContainer.fromPacket(equipmentPacket));

        var watcher = parameters.getWatcher();
        watcher.sync();
        packets.add(buildMetaPacket(player, watcher));

        return packets;
    }

    public PacketContainer buildMetaPacket(Player player, SingleWatcher watcher)
    {
        var metaPacket = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        metaPacket.getIntegers().write(0, player.getEntityId());

        var modifier = metaPacket.getDataValueCollectionModifier();

        var entityWatcher = WrappedDataWatcher.getEntityWatcher(player);
        entityWatcher.asMap().forEach((id, val) ->
        {
            //logger.info("Id '%s' is val '%s' raw '%s' class '%s'"
            //        .formatted(id, val.getValue(),val.getRawValue(),val.getRawValue().getClass()));
        });

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
            //logger.info("Writing value '%s' index '%s'".formatted(v, single.index()));
        });

        modifier.write(0, wrappedDataValues);
        metaPacket.setMeta(MORPH_PACKET_METAKEY, true);

        return metaPacket;
    }
}
