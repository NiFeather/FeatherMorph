package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import net.kyori.adventure.text.Component;

public class TextDisplayValues extends DisplayEntityValues
{
    public final SingleValue<Component> TEXT = createSingle("text_display_text", Component.empty(), EntityDataTypes.ADV_COMPONENT);
    public final SingleValue<Integer> LINE_WIDTH = createSingle("text_display_line_width", 200, EntityDataTypes.INT);
    public final SingleValue<Integer> BACKGROUND_COLOR = createSingle("text_display_background_color", 1073741824, EntityDataTypes.INT);
    public final SingleValue<Byte> TEXT_OPACITY = createSingle("text_display_text_opacity", (byte) -1, EntityDataTypes.BYTE);
    public final SingleValue<Byte> ALIGN_FLAG = createSingle("text_display_align_flag", (byte) 0, EntityDataTypes.BYTE);

    public TextDisplayValues()
    {
        super();

        registerSingle(TEXT, LINE_WIDTH, BACKGROUND_COLOR, TEXT_OPACITY, ALIGN_FLAG);
    }
}
