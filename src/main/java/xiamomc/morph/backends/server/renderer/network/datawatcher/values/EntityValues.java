package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Pose;

import java.util.Optional;

public class EntityValues extends AbstractValues
{
    public final SingleValue<Byte> GENERAL = getSingle((byte)0);
    public final SingleValue<Integer> AIR_TICKS = getSingle(0);

    @Deprecated
    public final SingleValue<Optional<Component>> CUSTOMNAME = getSingle(Optional.of(Component.empty()));

    public final SingleValue<Boolean> CUSTOMNAME_VISIBLE = getSingle(false);
    public final SingleValue<Boolean> SILENT = getSingle(false);
    public final SingleValue<Boolean> NO_GRAVITY = getSingle(false);
    public final SingleValue<Pose> POSE = getSingle(Pose.STANDING);
    public final SingleValue<Integer> FROZEN_TICKS = getSingle(0);

    public EntityValues()
    {
        registerSingle(GENERAL, AIR_TICKS, CUSTOMNAME, CUSTOMNAME_VISIBLE, SILENT, NO_GRAVITY,
                POSE, FROZEN_TICKS);
    }
}
