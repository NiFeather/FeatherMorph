package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.core.Direction;

public class ShulkerValues extends MonsterValues
{
    public final SingleValue<Direction> ATTACH_FACE_ID = getSingle("shulker_attach_face_id", Direction.DOWN);
    public final SingleValue<Byte> PEEK_ID = getSingle("shulker_peek_id", (byte)0);
    public final SingleValue<Byte> COLOR_ID = getSingle("shulker_color_id", (byte)16);

    public ShulkerValues()
    {
        registerSingle(ATTACH_FACE_ID, PEEK_ID, COLOR_ID);
    }
}
