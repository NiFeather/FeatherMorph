package xiamomc.morph.misc;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.EntityPose;
import org.bukkit.entity.Pose;

public class DisguiseUtils
{
    private static final String customDataTagName = "XIAMO_MORPH";

    public static void addTrace(Disguise disguise)
    {
        disguise.addCustomData(customDataTagName, true);
    }

    public static boolean isTracing(Disguise disguise)
    {
        return Boolean.TRUE.equals(disguise.getCustomData(customDataTagName));
    }

    public static EntityPose toEntityPose(Pose pose)
    {
        return switch (pose)
        {
            case SWIMMING -> EntityPose.SWIMMING;
            case FALL_FLYING -> EntityPose.FALL_FLYING;
            case SNEAKING -> EntityPose.SNEAKING;
            case SLEEPING -> EntityPose.SLEEPING;
            case SPIN_ATTACK -> EntityPose.SPIN_ATTACK;
            case DYING -> EntityPose.DYING;
            default -> EntityPose.STANDING;
        };
    }
}
