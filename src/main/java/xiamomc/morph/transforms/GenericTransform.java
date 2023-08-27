package xiamomc.morph.transforms;

import xiamomc.morph.transforms.clock.IClock;
import xiamomc.morph.transforms.easings.Easing;

public class GenericTransform<TValue> extends Transform<TValue>
{
    public Recorder<TValue> val;

    protected GenericTransform(Recorder<TValue> recorder, long startTime, long duration, TValue endValue, Easing easing, IClock clock)
    {
        super(startTime, duration, recorder.get(), endValue, easing, clock);

        this.val = recorder;
    }

    public void update(Recorder<TValue> recorder, long startTime, long duration, TValue endValue, Easing easing)
    {
        super.update(startTime, duration, recorder.get(), endValue, easing);
    }

    @Override
    public void applyProgress(double timeProgress)
    {
        TValue value;

        if (timeProgress >= 1) value = endValue;
        else if (timeProgress < 0) value = startValue;
        else value = TransformUtils.valueAt(timeProgress, startValue, endValue, easing);

        this.val.set(value);
    }
}

