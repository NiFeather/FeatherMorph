package xyz.nifeather.morph.misc;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.Pair;
import xyz.nifeather.morph.misc.actions.BiConsumerActions;
import xyz.nifeather.morph.misc.actions.ConsumerActions;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfirmationHandler<E>
{
    private final Map<E, Long> confirmationMap = new ConcurrentHashMap<>();

    private final BiConsumerActions<E, Duration> onSubmit = new BiConsumerActions<>();
    public BiConsumerActions<E, Duration> onSubmit()
    {
        return onSubmit;
    }

    private final ConsumerActions<E> onExpire = new ConsumerActions<>();
    public ConsumerActions<E> onExpire()
    {
        return onExpire;
    }

    public boolean contains(E handle)
    {
        return confirmationMap.containsKey(handle);
    }

    public void submit(E handle, Duration duration)
    {
        var time = System.currentTimeMillis() + duration.toMillis();
        confirmationMap.put(handle, time);

        onSubmit.invoke(Pair.of(handle, duration));
    }

    public boolean confirm(E handle)
    {
        return confirmationMap.remove(handle) != null;
    }

    public boolean expire(E handle)
    {
        if (!contains(handle))
            return false;

        confirmationMap.remove(handle);
        onExpire.invoke(handle);

        return true;
    }

    public void update()
    {
        var current = System.currentTimeMillis();

        var map = ImmutableMap.copyOf(confirmationMap);
        map.forEach((e, time) ->
        {
            if (current > time)
                expire(e);
        });
    }
}
