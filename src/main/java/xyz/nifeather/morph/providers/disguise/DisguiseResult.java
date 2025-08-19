package xyz.nifeather.morph.providers.disguise;

import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.backends.DisguiseWrapper;

/**
 * @param wrapperInstance Wrapper实例，在失败时为空
 * @param success 操作是否成功
 */
public record DisguiseResult(@Nullable DisguiseWrapper<?> wrapperInstance, boolean success, boolean failSilent)
{
    public static final DisguiseResult FAIL = new DisguiseResult(null, false, false);
    public static final DisguiseResult FAIL_SILENT = new DisguiseResult(null, false, true);

    public static DisguiseResult fail()
    {
        return DisguiseResult.FAIL;
    }

    public static DisguiseResult success(DisguiseWrapper<?> disguise)
    {
        return new DisguiseResult(disguise, true, false);
    }
}
