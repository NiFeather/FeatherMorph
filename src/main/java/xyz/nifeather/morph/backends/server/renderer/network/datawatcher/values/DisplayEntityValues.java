package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.util.Quaternion4f;
import com.github.retrooper.packetevents.util.Vector3f;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes.EntityValues;

public class DisplayEntityValues extends EntityValues
{
    // Display entity common
    public final SingleValue<Integer> TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS = createSingle("display_transformation_interpolation_start_delta_ticks", 0, EntityDataTypes.INT);
    public final SingleValue<Integer> TRANSFORMATION_INTERPOLATION_DURATION = createSingle("display_transformation_interpolation_duration", 0, EntityDataTypes.INT);
    public final SingleValue<Integer> POS_ROT_INTERPOLATION_DURATION = createSingle("display_pos_rot_interpolation_duration", 0, EntityDataTypes.INT);
    public final SingleValue<Vector3f> TRANSLATION = createSingle("display_translation", Vector3f.zero(), EntityDataTypes.VECTOR3F);
    public final SingleValue<Vector3f> SCALE = createSingle("display_scale", new Vector3f(1, 1, 1), EntityDataTypes.VECTOR3F);
    public final SingleValue<Quaternion4f> LEFT_ROTATION = createSingle("display_left_rotation", new Quaternion4f(0 ,0, 0, 0), EntityDataTypes.QUATERNION);
    public final SingleValue<Quaternion4f> RIGHT_ROTATION = createSingle("display_right_rotation", new Quaternion4f(0 ,0, 0, 0), EntityDataTypes.QUATERNION);
    public final SingleValue<Byte> BILLBOARD_RENDER_CONSTRAINTS = createSingle("display_billboard_render_constraints", (byte)0, EntityDataTypes.BYTE);
    public final SingleValue<Integer> BRIGHTNESS_OVERRIDE = createSingle("display_brightness_override", 0, EntityDataTypes.INT);
    public final SingleValue<Float> VIEW_RANGE = createSingle("display_view_range", 16f, EntityDataTypes.FLOAT);
    public final SingleValue<Float> SHADOW_RANGE = createSingle("display_shadow_range", 0f, EntityDataTypes.FLOAT);
    public final SingleValue<Float> SHADOW_STRENGTH = createSingle("display_shadow_strength", 1f, EntityDataTypes.FLOAT);

    public final SingleValue<Float> CULLING_BOUNDINGBOX_WIDTH = createSingle("display_width", 0f, EntityDataTypes.FLOAT);
    public final SingleValue<Float> CULLING_BOUNDINGBOX_HEIGHT = createSingle("display_height", 0f, EntityDataTypes.FLOAT);

    public final SingleValue<Integer> GLOW_COLOR_OVERRIDE = createSingle("display_glow_color_override", -1, EntityDataTypes.INT);

    public DisplayEntityValues()
    {
        super();

        registerSingle(
                POS_ROT_INTERPOLATION_DURATION,
                TRANSFORMATION_INTERPOLATION_START_DELTA_TICKS,
                TRANSFORMATION_INTERPOLATION_DURATION,
                TRANSLATION,
                SCALE,
                RIGHT_ROTATION,
                LEFT_ROTATION,
                BILLBOARD_RENDER_CONSTRAINTS,
                BRIGHTNESS_OVERRIDE,
                VIEW_RANGE,
                SHADOW_RANGE,
                SHADOW_STRENGTH,
                CULLING_BOUNDINGBOX_WIDTH,
                CULLING_BOUNDINGBOX_HEIGHT,
                GLOW_COLOR_OVERRIDE
        );
    }
}
