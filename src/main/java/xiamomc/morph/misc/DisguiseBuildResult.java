package xiamomc.morph.misc;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.providers.disguise.DisguiseProvider;

import java.util.Objects;

/**
 *
 * @param success 是否成功
 * @param state 生产出来的DisguiseState
 * @param provider 对应的DisguiseProvider
 * @param meta 对应的DisguiseMeta
 */
public record DisguiseBuildResult(boolean success,
                                   DisguiseState state,
                                   DisguiseProvider provider,
                                   DisguiseMeta meta,
                                   @Nullable Entity targetedEntity)
{
    public static final DisguiseBuildResult FAILED = new DisguiseBuildResult(false, null, null, null, null);

    public static DisguiseBuildResult of(DisguiseState state, DisguiseProvider provider, DisguiseMeta meta, @Nullable Entity targetedEntity)
    {
        Objects.requireNonNull(state, "Null state!");
        Objects.requireNonNull(provider, "Null provider!");
        Objects.requireNonNull(meta, "Null Meta!");

        return new DisguiseBuildResult(true, state, provider, meta, targetedEntity);
    }
}
