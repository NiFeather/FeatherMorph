package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import net.kyori.adventure.text.Component;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.AbstractValues;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

import java.util.Optional;

public class EntityValues extends AbstractValues
{
    public final SingleValue<Byte> GENERAL = createSingle("entity_general", (byte)0, EntityDataTypes.BYTE);
    public final SingleValue<Integer> AIR_TICKS = createSingle("entity_air_ticks", 0, EntityDataTypes.INT);
    public final SingleValue<Optional<Component>> CUSTOM_NAME = createSingle("entity_custom_name", Optional.empty(), EntityDataTypes.OPTIONAL_ADV_COMPONENT);
    public final SingleValue<Boolean> CUSTOM_NAME_VISIBLE = createSingle("entity_custom_name_visible", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Boolean> SILENT = createSingle("entity_silent", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Boolean> NO_GRAVITY = createSingle("entity_no_gravity", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<EntityPose> POSE = createSingle("entity_pose", EntityPose.STANDING, EntityDataTypes.ENTITY_POSE);
    public final SingleValue<Integer> FROZEN_TICKS = createSingle("entity_frozen_ticks", 0, EntityDataTypes.INT);

    public EntityValues()
    {
        registerSingle(GENERAL, AIR_TICKS, CUSTOM_NAME, CUSTOM_NAME_VISIBLE, SILENT, NO_GRAVITY,
                POSE, FROZEN_TICKS);
    }
}
