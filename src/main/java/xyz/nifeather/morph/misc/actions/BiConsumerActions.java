package xyz.nifeather.morph.misc.actions;

import it.unimi.dsi.fastutil.Pair;

import java.util.function.BiConsumer;

public class BiConsumerActions<A, B> extends CallableActions<Pair<A, B>, BiConsumer<A, B>>
{
    public static <A, B> Pair<A, B> pair(A a, B b)
    {
        return Pair.of(a, b);
    }

    @Override
    public void invoke(Pair<A, B> value)
    {
        this.hooks.forEach(bic -> bic.accept(value.left(), value.right()));
    }
}
