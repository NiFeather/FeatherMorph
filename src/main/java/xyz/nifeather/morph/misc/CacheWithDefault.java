package xyz.nifeather.morph.misc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CacheWithDefault<C>
{
    @NotNull
    private final C defaultVal;

    @Nullable
    private C val;

    public CacheWithDefault(@NotNull C defaultVal)
    {
        this.defaultVal = defaultVal;
    }

    public C get()
    {
        return val == null ? defaultVal : val;
    }

    public C set(C val)
    {
        this.val = val;
        return val;
    }

    public static <X> CacheWithDefault<X> of(X val)
    {
        return new CacheWithDefault<>(val);
    }
}
