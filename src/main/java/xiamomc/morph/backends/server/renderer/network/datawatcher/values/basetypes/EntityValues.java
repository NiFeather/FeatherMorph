package xiamomc.morph.backends.server.renderer.network.datawatcher.values.basetypes;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Pose;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.AbstractValues;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;

import java.util.Optional;

public class EntityValues extends AbstractValues
{
    public final SingleValue<Byte> GENERAL = getSingle("entity_general", (byte)0);
    public final SingleValue<Integer> AIR_TICKS = getSingle("entity_air_ticks", 0);
    public final SingleValue<Optional<Component>> CUSTOM_NAME = getSingle("entity_custom_name", Optional.empty());
    public final SingleValue<Boolean> CUSTOM_NAME_VISIBLE = getSingle("entity_custom_name_visible", false);
    public final SingleValue<Boolean> SILENT = getSingle("entity_silent", false);
    public final SingleValue<Boolean> NO_GRAVITY = getSingle("entity_no_gravity", false);
    public final SingleValue<Pose> POSE = getSingle("entity_pose", Pose.STANDING);
    public final SingleValue<Integer> FROZEN_TICKS = getSingle("entity_frozen_ticks", 0);

    //TODO: USE ADVENTURE COMPONENT FOR SERIALIZE
    public EntityValues()
    {
        CUSTOM_NAME.setSerializer(WrappedDataWatcher.Registry.getChatComponentSerializer(true));

        registerSingle(GENERAL, AIR_TICKS, CUSTOM_NAME, CUSTOM_NAME_VISIBLE, SILENT, NO_GRAVITY,
                POSE, FROZEN_TICKS);
    }
}
