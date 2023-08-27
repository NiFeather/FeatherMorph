package xiamomc.morph.transforms;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.transforms.clock.IClock;
import xiamomc.morph.transforms.clock.SystemClock;
import xiamomc.morph.transforms.easings.Easing;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.List;

public class Transformer extends MorphPluginObject
{
    @Initializer
    private void load()
    {
        this.addSchedule(this::onTickEnd);
    }

    public static void update(Transform<?> t)
    {
        update(t, false);
    }

    /**
     * 立即更新一次Transform
     * @param t
     */
    public static void update(Transform<?> t, boolean tickAborted)
    {
        if (t.aborted && !tickAborted)
        {
            transforms.remove(t);
            return;
        }

        var currentTime = t.clock.getCurrentTimeMills();

        double timeProgress = (currentTime - t.startTime) * 1d / t.duration;

        t.applyProgress(timeProgress);

        //如果进度不等于1，那么继续
        if (timeProgress < 1) return;

        transforms.remove(t);

        if (t.onFinish == null) return;

        for (Runnable onFinish : t.onFinish)
        {
            try
            {
                onFinish.run();
            }
            catch (Throwable throwable)
            {
                MorphPlugin.getInstance().getSLF4JLogger().warn(throwable.getMessage());
                throwable.printStackTrace();
            }
        }
    }

    public void onTickEnd()
    {
        this.addSchedule(this::onTickEnd);

        var transformList = new ObjectArrayList<>(transforms);
        transformList.forEach(Transformer::update);
    }

    private static final List<Transform<?>> transforms = new ObjectArrayList<>();

    public static synchronized void startTransform(Transform<?> info)
    {
        transforms.add(info);
    }

    public static <T> GenericTransform<T> transform(Recorder<T> recorder, T endValue, long duration, Easing easing)
    {
        return transform(recorder, endValue, duration, easing, SystemClock.INSTANCE);
    }

    public static <T> GenericTransform<T> transform(Recorder<T> recorder, T endValue, long duration, Easing easing, IClock defaultClock)
    {
        var prevTransform = (GenericTransform<T>) transforms.stream()
                .filter(t -> (t instanceof GenericTransform<?> tB && tB.val == recorder))
                .findFirst().orElse(null);

        if (prevTransform != null)
        {
            prevTransform.update(recorder, prevTransform.clock.getCurrentTimeMills(), duration, endValue, easing);
            return prevTransform;
        }
        else
        {
            var transform = new GenericTransform<>(recorder, defaultClock.getCurrentTimeMills(), duration, endValue, easing, defaultClock);
            startTransform(transform);
            return transform;
        }
    }

    public static DelayTransform delay(int duration)
    {
        return delay(duration, SystemClock.INSTANCE);
    }

    public static DelayTransform delay(int duration, IClock clock)
    {
        var transform = new DelayTransform(clock.getCurrentTimeMills(), duration, clock);
        startTransform(transform);

        return transform;
    }

    public static <T> BindableTransform<T> transform(Bindable<T> bindable, T endValue, long duration, Easing easing)
    {
        return transform(bindable, endValue, duration, easing, SystemClock.INSTANCE);
    }

    public static <T> BindableTransform<T> transform(Bindable<T> bindable, T endValue, long duration, Easing easing, IClock defaultClock)
    {
        var prevTransform = (BindableTransform<T>) transforms.stream()
                .filter(t -> (t instanceof BindableTransform<?> tB && tB.bindable == bindable))
                .findFirst().orElse(null);

        if (prevTransform != null)
        {
            prevTransform.update(bindable, prevTransform.clock.getCurrentTimeMills(), duration, endValue, easing);

            return prevTransform;
        }
        else
        {
            var info = new BindableTransform<>(bindable, defaultClock.getCurrentTimeMills(), duration, endValue, easing, defaultClock);
            startTransform(info);
            return info;
        }
    }
}
