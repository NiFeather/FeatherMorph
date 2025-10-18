package xyz.nifeather.morph.misc;

import java.util.Objects;

/**
 *
 * @param success 是否成功
 * @param state 生产出来的DisguiseState
 * @param meta 对应的DisguiseMeta
 */
public record DisguiseBuildResult(boolean success,
                                   DisguiseState state,
                                   DisguiseMeta meta)
{
    public static final DisguiseBuildResult FAILED = new DisguiseBuildResult(false, null, null);

    public static DisguiseBuildResult of(DisguiseState state, DisguiseMeta meta)
    {
        Objects.requireNonNull(state, "Null state!");
        Objects.requireNonNull(meta, "Null Meta!");

        return new DisguiseBuildResult(true, state, meta);
    }
}
