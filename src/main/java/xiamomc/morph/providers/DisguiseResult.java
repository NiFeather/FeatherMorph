package xiamomc.morph.providers;

import org.jetbrains.annotations.Nullable;
import xiamomc.morph.backends.DisguiseWrapper;

public record DisguiseResult(@Nullable DisguiseWrapper<?> disguise, boolean success, boolean isCopy)
{
    public static DisguiseResult fail()
    {
        return new DisguiseResult(null ,false, false);
    }

    public static DisguiseResult success(DisguiseWrapper<?> disguise, boolean isCopy)
    {
        return new DisguiseResult(disguise, true, isCopy);
    }

    public static DisguiseResult success(DisguiseWrapper<?> disguise)
    {
        return new DisguiseResult(disguise, true, false);
    }
}
