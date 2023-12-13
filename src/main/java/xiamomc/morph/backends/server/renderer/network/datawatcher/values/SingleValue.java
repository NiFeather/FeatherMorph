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

    public static <TVal> SingleValue<TVal> of(int index, @NotNull TVal val)
    {
        if (val == null)
            throw new IllegalArgumentException("TVal may not be null");

        return new SingleValue<>((Class<TVal>) val.getClass(), index, val);
    }
}
