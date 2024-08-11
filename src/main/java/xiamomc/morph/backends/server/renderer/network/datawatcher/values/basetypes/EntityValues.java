package xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import net.kyori.adventure.text.Component;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.AbstractValues;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

import java.util.Optional;

public class EntityValues extends AbstractValues
{
    public final SingleValue<Byte> GENERAL = getSingle("general", (byte)0, EntityDataTypes.BYTE);
    public final SingleValue<Integer> AIR_TICKS = getSingle("air_ticks", 0, EntityDataTypes.INT);
    public final SingleValue<Optional<Component>> CUSTOM_NAME = getSingle("custom_name", Optional.of(Component.empty()),EntityDataTypes.OPTIONAL_ADV_COMPONENT);
    public final SingleValue<Boolean> CUSTOM_NAME_VISIBLE = getSingle("custom_name_visible", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Boolean> SILENT = getSingle("silent", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<Boolean> NO_GRAVITY = getSingle("no_gravity", false, EntityDataTypes.BOOLEAN);
    public final SingleValue<EntityPose> POSE = getSingle("pose", EntityPose.STANDING, EntityDataTypes.ENTITY_POSE);
    public final SingleValue<Integer> FROZEN_TICKS = getSingle("frozen_ticks", 0, EntityDataTypes.INT);

    public EntityValues()
    {
        registerSingle(GENERAL, AIR_TICKS, CUSTOM_NAME, CUSTOM_NAME_VISIBLE, SILENT, NO_GRAVITY,
                POSE, FROZEN_TICKS);
    }
}
