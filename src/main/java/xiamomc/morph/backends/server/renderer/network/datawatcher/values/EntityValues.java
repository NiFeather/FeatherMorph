package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Pose;

import java.util.Optional;

public class EntityValues extends AbstractValues
{
    public final SingleValue<Byte> GENERAL = SingleValue.of(0, (byte)0);
    public final SingleValue<Integer> AIR_TICKS = SingleValue.of(1, 0);

    @Deprecated
    public final SingleValue<Optional<Component>> CUSTOMNAME = SingleValue.of(2, Optional.of(Component.empty()));

    public final SingleValue<Boolean> CUSTOMNAME_VISIBLE = SingleValue.of(3, false);
    public final SingleValue<Boolean> SILENT = SingleValue.of(4, false);
    public final SingleValue<Boolean> NO_GRAVITY = SingleValue.of(5, false);
    public final SingleValue<Pose> POSE = SingleValue.of(6, Pose.STANDING);
    public final SingleValue<Integer> FROZEN_TICKS = SingleValue.of(7, 0);

    public EntityValues()
    {
        registerValue(GENERAL, AIR_TICKS, CUSTOMNAME, CUSTOMNAME_VISIBLE, SILENT, NO_GRAVITY,
                POSE, FROZEN_TICKS);
    }
}
