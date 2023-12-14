package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SingleValue<T>
{
    private final Class<T> type;

    public Class<T> type()
    {
        return type;
    }

    private final int index;

    public int index()
    {
        return index;
    }

    private final T defaultValue;

    @NotNull
    public T defaultValue()
    {
        return defaultValue;
    }

    public SingleValue(Class<T> type, int index, @NotNull T defaultValue)
    {
        this.type = type;
        this.index = index;
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof SingleValue<?> other)) return false;

        return this.index == other.index && this.type.equals(other.type);
    }

    @Override
    public String toString()
    {
        return "SingleValue[type='%s', index='%s']".formatted(type, index);
    }

    public static <TVal> SingleValue<TVal> of(int index, @NotNull TVal val)
    {
        if (val == null)
            throw new IllegalArgumentException("TVal may not be null");

        return new SingleValue<>((Class<TVal>) val.getClass(), index, val);
    }
}
