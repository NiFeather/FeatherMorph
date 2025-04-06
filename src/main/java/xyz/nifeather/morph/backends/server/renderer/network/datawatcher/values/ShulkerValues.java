package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.world.BlockFace;

public class ShulkerValues extends MonsterValues
{
    public final SingleValue<BlockFace> ATTACH_FACE_ID = createSingle("shulker_attach_face_id", BlockFace.DOWN, EntityDataTypes.BLOCK_FACE);
    public final SingleValue<Byte> PEEK_ID = createSingle("shulker_peek_id", (byte)0, EntityDataTypes.BYTE);
    public final SingleValue<Byte> COLOR_ID = createSingle("shulker_color_id", (byte)16, EntityDataTypes.BYTE);

    public ShulkerValues()
    {
        registerSingle(ATTACH_FACE_ID, PEEK_ID, COLOR_ID);
    }
}
