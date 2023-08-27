package xiamomc.morph.transforms;

import xiamomc.morph.transforms.clock.IClock;
import xiamomc.morph.transforms.easings.Easing;

public class DelayTransform extends Transform<Long>
{
    protected DelayTransform(long startTime, long duration, IClock clock)
    {
        super(startTime, duration, 0L, 100L, Easing.Plain, clock);
    }

    /**
     * Applies a time point to this transformation
     *
     * @param timeProgress The time progress (From 0 to 1)
     * @apiNote The value should be capped within the range (0% ~ 100%) if it exceeds.
     */
    @Override
    public void applyProgress(double timeProgress)
    {
    }
}
