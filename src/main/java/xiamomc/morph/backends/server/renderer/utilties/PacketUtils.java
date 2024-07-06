package xiamomc.morph.backends.server.renderer.utilties;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketUtils
{
    public static final int METADATA_INDEX = 995880325;
    public static final int ITEM_AMOUNT = 86;
    private static final Logger log = LoggerFactory.getLogger(PacketUtils.class);

    public static void removeMark(WrapperPlayServerEntityMetadata metadataWrapper)
    {
        metadataWrapper.getEntityMetadata().removeIf(data -> data.getIndex() == METADATA_INDEX);
    }

    public static void removeMark(WrapperPlayServerEntityEquipment equipmentWrapper)
    {
        var equip = equipmentWrapper.getEquipment().stream()
                .filter(eq -> eq.getItem().getAmount() == ITEM_AMOUNT)
                .findFirst().orElse(null);

        if (equip == null) return;

        equip.getItem().setAmount(1);
    }

    public static void removeMark(WrapperPlayServerSpawnEntity spawnEntity)
    {
        if (spawnEntity.getData() == ENTITY_ID_MODIFIER)
            spawnEntity.setData(0);
    }

    public static boolean isPacketOurs(WrapperPlayServerEntityMetadata metadataWrapper)
    {
        return metadataWrapper.getEntityMetadata().stream().anyMatch(data -> data.getIndex() == METADATA_INDEX);
    }

    public static boolean isPacketOurs(WrapperPlayServerEntityEquipment equipmentWrapper)
    {
        return equipmentWrapper.getEquipment().stream().anyMatch(eq -> eq.getItem().getAmount() == ITEM_AMOUNT);
    }

    public static final int ENTITY_ID_MODIFIER = 77890234;

    public static boolean isPacketOurs(WrapperPlayServerSpawnEntity spawnEntity)
    {
        return spawnEntity.getData() == ENTITY_ID_MODIFIER;
    }

    public static void markPacketOurs(WrapperPlayServerSpawnEntity spawnEntity)
    {
        if (spawnEntity.getData() != 0)
            log.warn("The original packet have a non-zero entity data, we are going to overwrite it!");

        spawnEntity.setData(ENTITY_ID_MODIFIER);
    }

    public static void markPacketOurs(WrapperPlayServerEntityMetadata metadataWrapper)
    {
        metadataWrapper.getEntityMetadata().add(new EntityData(METADATA_INDEX, EntityDataTypes.INT, 0));
    }

    public static void markPacketOurs(WrapperPlayServerEntityEquipment equipmentWrapper)
    {
        var equipList = equipmentWrapper.getEquipment();

        if (equipList.isEmpty())
        {
            log.warn("Marking an empty equipment packet as ours is not possible right now!!!");
            return;
        }

        equipList.get(0).getItem().setAmount(ITEM_AMOUNT);
    }
}
