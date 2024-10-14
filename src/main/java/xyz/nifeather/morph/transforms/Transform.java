package xyz.nifeather.morph.transforms;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import xyz.nifeather.morph.transforms.easings.Easing;

import java.util.List;

public abstract class Transform<TValue>
{
    protected Transform(long startTime, long duration, TValue startValue, TValue endValue, Easing easing)
    {
        this.update(startTime, duration, startValue, endValue, easing);
    }

    public void update(long startTime, long duration, TValue startValue, TValue endValue, Easing easing)
    {
        this.startTime = startTime;
        this.duration = duration;

        this.startValue = startValue;
        this.endValue = endValue;

        this.easing = easing;
    }

    public void abort()
    {
        this.aborted = true;
    }

    public long startTime;
    public long duration;

    public TValue startValue;
    public TValue endValue;

    public Easing easing;

    public boolean aborted;

    /**
     * Applies a time point to this transformation
     * @param timeProgress The time progress (From 0 to 1)
     * @apiNote The value should be capped within the range (0% ~ 100%) if it exceeds.
     */
    public abstract void applyProgress(double timeProgress);

    public List<Runnable> onFinish;

    public Transform<TValue> then(Runnable runnable)
    {
        if (this.onFinish == null) onFinish = new ObjectArrayList<>();

        this.onFinish.add(runnable);

        return this;
    }
}
