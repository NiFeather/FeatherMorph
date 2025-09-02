package xyz.nifeather.morph.misc.actions;

import java.util.function.Consumer;

public class ConsumerActions<X> extends CallableActions<X, Consumer<X>>
{
    @Override
    public void invoke(X value)
    {
        hooks.forEach(c -> c.accept(value));
    }
}
