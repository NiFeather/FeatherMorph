package xiamomc.morph.backends.server.renderer.network;

import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.AbstractValues;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.utilties.PacketUtils;
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

public class PacketFactory extends MorphPluginObject
{
    private final Bindable<String> randomBase = new Bindable<>("Stateof");

    @Initializer
    private void load(MorphConfigManager config)
    {
        config.bind(randomBase, ConfigOption.UUID_RANDOM_BASE);
    }

    public List<PacketWrapper<?>> buildSpawnPackets(Player player, DisplayParameters parameters)
    {
        List<PacketWrapper<?>> packets = new ObjectArrayList<>();

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
                var str = randomBase.get() + player.getName();
                gameProfile.setUUID(UUID.nameUUIDFromBytes(str.getBytes()));
            }

            // 如果此伪装在TAB里留有一个Entry，那么移除它。
            var lastUUID = parameters.getWatcher().getOrDefault(EntryIndex.TABLIST_UUID, null);

            if (lastUUID != null)
            {
                gameProfile.setUUID(lastUUID);
                packets.add(new WrapperPlayServerPlayerInfoRemove(List.of(lastUUID)));
            }

            //Minecraft需要在生成玩家实体前先发送PlayerInfoUpdate消息
            var uuid = gameProfile.getId();

            var profileName = gameProfile.getName();
            if (profileName.length() > 16)
            {
                logger.warn("Profile name '%s' exceeds the maximum length 16!".formatted(profileName));
                var subStr = profileName.substring(0, 15);
                gameProfile.setName(subStr);
            }

            if (gameProfile.getName().isBlank())
                throw new IllegalArgumentException("GameProfile name is empty!");

            var packetPlayerInfo = new WrapperPlayServerPlayerInfoUpdate(
                    EnumSet.of(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER),
                    new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                        MorphGameProfile.toPacketEventsUserProfile(gameProfile), false, 114514,
                            GameMode.CREATIVE, net.kyori.adventure.text.Component.text("???"), null
                    ));

            spawnUUID = uuid;
            packets.add(packetPlayerInfo);

            parameters.getWatcher().write(EntryIndex.TABLIST_UUID, uuid);
        }

        var pitch = player.getPitch();
        var yaw = player.getYaw();

        if (disguiseType == EntityType.PHANTOM)
            pitch = -player.getPitch();

        if (disguiseType == EntityType.ENDER_DRAGON)
            yaw = 180 + yaw;

        //生成实体
        var playerVelocity = player.getVelocity();
        var spawnPacket = new WrapperPlayServerSpawnEntity(
                player.getEntityId(), spawnUUID,
                SpigotConversionUtil.fromBukkitEntityType(disguiseType), SpigotConversionUtil.fromBukkitLocation(player.getLocation()),
                yaw, 0, new Vector3d(playerVelocity.getX(), playerVelocity.getY(), playerVelocity.getZ())
        );
        PacketUtils.markPacketOurs(spawnPacket);

        packets.add(spawnPacket);

        var watcher = parameters.getWatcher();

        //生成装备和Meta
        var displayingFake = watcher.getOrDefault(EntryIndex.DISPLAY_FAKE_EQUIPMENT, false);
        var equip = displayingFake
                ? watcher.getOrDefault(EntryIndex.EQUIPMENT, new DisguiseEquipment())
                : player.getEquipment();

        var equipmentPacket = new WrapperPlayServerEntityEquipment(player.getEntityId(),
                ProtocolEquipment.toPacketEventsEquipmentList(equip));

        packets.add(equipmentPacket);

        if (parameters.includeMeta())
            packets.add(buildFullMetaPacket(player, parameters.getWatcher()));

        Entity rootVehicle = null;
        for (var vehicle = player.getVehicle(); vehicle != null; vehicle = vehicle.getVehicle())
            rootVehicle = vehicle;

        if (rootVehicle != null)
        {
            var passengers = rootVehicle.getPassengers().stream()
                    .mapToInt(Entity::getEntityId)
                    .toArray();

            packets.add(new WrapperPlayServerSetPassengers(rootVehicle.getEntityId(), passengers));
        }

        var bukkitEntityType = parameters.getEntityType();
        if (bukkitEntityType.isAlive())
        {
            //Attributes
            List<AttributeInstance> attributes = bukkitEntityType == EntityType.PLAYER
                    ? new ObjectArrayList<>(nmsPlayer.getAttributes().getSyncableAttributes())
                    : NmsUtils.getValidAttributes(bukkitEntityType, nmsPlayer.getAttributes());

            List<WrapperPlayServerUpdateAttributes.Property> propertyList = new ObjectArrayList<>();
            for (AttributeInstance attribute : attributes)
            {
                var packetAttribute = Attributes.getByName(attribute.getAttribute().getRegisteredName());
                if (packetAttribute == null)
                {
                    logger.warn("Local attribute '%s' has no packet version!".formatted(attribute.getAttribute().getRegisteredName()));
                    continue;
                }

                List<WrapperPlayServerUpdateAttributes.PropertyModifier> modifiers = new ObjectArrayList<>();
                for (AttributeModifier modifier : attribute.getModifiers())
                {
                    var packetModifier = new WrapperPlayServerUpdateAttributes.PropertyModifier(
                            new ResourceLocation(modifier.id().getNamespace(), modifier.id().getPath()),
                            modifier.amount(),
                            WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.values()[modifier.operation().ordinal()]
                    );

                    modifiers.add(packetModifier);
                }

                propertyList.add(new WrapperPlayServerUpdateAttributes.Property(packetAttribute, attribute.getValue(), modifiers));
            }

            var attributePacket = new WrapperPlayServerUpdateAttributes(player.getEntityId(), propertyList);
            packets.add(attributePacket);
        }

        return packets;
    }

    /**
     * 从给定的meta包中移除不属于给定AbstractValues的数据
     * @return 剔除后的包
     */
    public List<EntityData> removeNonLivingValues(AbstractValues av, List<EntityData> originalData)
    {
        var values = av.getValues();

        //剔除不属于给定AbstractValues中的数据
        originalData.removeIf(w ->
        {
            var rawValue = w.getValue();

            var match = values.stream().filter(sv ->
                    w.getIndex() == sv.index() && (rawValue == null || rawValue.getClass() == sv.defaultValue().getClass())
            ).findFirst().orElse(null);

            return match == null;
        });

        return originalData;
    }

    public WrapperPlayServerEntityMetadata buildDiffMetaPacket(Player player, SingleWatcher watcher)
    {
        var metaPacket = new WrapperPlayServerEntityMetadata(player.getEntityId(), new ObjectArrayList<>());

        List<EntityData> wrappedDataValues = new ObjectArrayList<>();
        Map<SingleValue<?>, Object> watcherDirty = watcher.getDirty();

        watcherDirty.forEach((single, v) ->
        {
            var instance = new EntityData(single.index(), single.getDataType(), v);
            wrappedDataValues.add(instance);
        });

        metaPacket.setEntityMetadata(wrappedDataValues);
        PacketUtils.markPacketOurs(metaPacket);

        return metaPacket;
    }

    public WrapperPlayServerEntityMetadata buildFullMetaPacket(Player player, SingleWatcher watcher)
    {
        watcher.sync();

        var metaPacket = new WrapperPlayServerEntityMetadata(player.getEntityId(), new ObjectArrayList<>());

        List<EntityData> wrappedDataValues = new ObjectArrayList<>();

        Map<SingleValue<?>, Object> watcherRegistry = watcher.getRegistry();

        watcherRegistry.forEach((single, v) ->
        {
            var instance = new EntityData(single.index(), single.getDataType(), v);
            wrappedDataValues.add(instance);
        });

        PacketUtils.markPacketOurs(metaPacket);

        return metaPacket;
    }

    public List<Equipment> getPacketEquipmentList(Player player, SingleWatcher watcher)
    {
        var shouldDisplayFakeEquip = watcher.getOrDefault(EntryIndex.DISPLAY_FAKE_EQUIPMENT, false);
        EntityEquipment equipment = shouldDisplayFakeEquip
                    ? watcher.getOrDefault(EntryIndex.EQUIPMENT, new DisguiseEquipment())
                    : player.getEquipment();

        return ProtocolEquipment.toPacketEventsEquipmentList(equipment);
    }
}
