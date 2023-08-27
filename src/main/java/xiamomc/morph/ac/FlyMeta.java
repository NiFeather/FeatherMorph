package xiamomc.morph.ac;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.transforms.Recorder;
import xiamomc.morph.transforms.Transform;
import xiamomc.morph.transforms.clock.DecoupledTickClock;
import xiamomc.morph.transforms.clock.IClock;

public class FlyMeta
{
    public boolean isSprinting;
    public boolean wasSprinting;
    public boolean isRiptiding;
    public boolean wasRiptiding;
    public Location lastLegalPosition;

    public int ignoreNext;

    /**
     * 此玩家的疾行飞行倍率
     */
    public final Recorder<Double> flyMult = new Recorder<>(0d);

    @Nullable
    public Transform<?> flyMultTransform;

    /**
     * 此玩家的激流飞行倍率
     */
    public final Recorder<Double> riptideMult = new Recorder<>(0d);

    @Nullable
    public Transform<?> riptideMultTransform;


    public final DecoupledTickClock clock = new DecoupledTickClock();
}
