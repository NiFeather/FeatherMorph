package xiamomc.morph.providers;

import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.jetbrains.annotations.Nullable;

public record DisguiseResult(@Nullable Disguise disguise, boolean success, boolean isCopy)
{
    public static DisguiseResult fail()
    {
        return new DisguiseResult(null ,false, false);
    }

    public static DisguiseResult success(Disguise disguise, boolean isCopy)
    {
        return new DisguiseResult(disguise, true, isCopy);
    }

    public static DisguiseResult success(Disguise disguise)
    {
        return new DisguiseResult(disguise, true, false);
    }
}
