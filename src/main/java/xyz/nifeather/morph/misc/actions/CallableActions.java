package xyz.nifeather.morph.misc.actions;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Collections;
import java.util.List;

public abstract class CallableActions<V, C>
{
    protected final List<C> hooks = Collections.synchronizedList(new ObjectArrayList<>());

    public CallableActions<V, C> hook(C hook)
    {
        hooks.add(hook);
        return this;
    }

    public abstract void invoke(V value);

    public void clear()
    {
        hooks.clear();
    }
}
