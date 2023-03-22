package xiamomc.morph.backends;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;

public abstract class DisguiseBackend<TInstance, TWrapper extends DisguiseWrapper<TInstance>> extends MorphPluginObject
{
    public abstract DisguiseWrapper<TInstance> createInstance(@Nullable Player player, @NotNull Entity targetEntity);

    public abstract DisguiseWrapper<TInstance> createInstance(@Nullable Player player, EntityType entityType);

    public abstract DisguiseWrapper<TInstance> createPlayerInstance(@Nullable Player player, String targetPlayerName);

    public abstract DisguiseWrapper<TInstance> fromOfflineString(@Nullable Player player, String offlineStr);

    public abstract TInstance createRawInstance(Entity entity);

    public abstract boolean isDisguised(Entity target);

    @Nullable
    public abstract TWrapper getDisguise(Entity target);

    /**
     * 将某一玩家伪装成给定Wrapper中的实例
     * @param player 目标玩家
     * @param wrapper 目标Wrapper
     * @return 操作是否成功
     * @apiNote 传入的wrapper可能不是此后端产出的Wrapper
     */
    public abstract boolean disguise(Player player, DisguiseWrapper<?> wrapper);
    public abstract boolean unDisguise(Player player);
}
