package xiamomc.morph.providers;

import org.jetbrains.annotations.Nullable;
import xiamomc.morph.backends.DisguiseWrapper;

/**
 * @param disguise
 * @param success
 * @param isCopy 此伪装是否克隆自其他实体或其他玩家的伪装
 */
public record DisguiseResult(@Nullable DisguiseWrapper<?> disguise, boolean success, boolean isCopy)
{
    public static final DisguiseResult FAIL = new DisguiseResult(null, false, false);

    public static DisguiseResult fail()
    {
        return DisguiseResult.FAIL;
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
