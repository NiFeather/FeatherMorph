package xiamomc.morph.providers;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.DisguiseInfo;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.pluginbase.Annotations.Resolved;

public abstract class DisguiseProvider extends MorphPluginObject
{
    /**
     * 获取此DisguiseProvider的ID，此ID将被用于判断某个伪装是否属于此Provider
     *
     * @return ID
     */
    @NotNull
    public abstract String getIdentifier();

    /**
     * 为某个玩家应用或更新伪装
     * @param player 目标玩家
     * @param disguiseInfo 伪装ID
     * @param targetEntity 玩家的目标实体(如果有), 可用来判断是否要复制伪装
     * @return 操作结果
     */
    @NotNull
    public abstract DisguiseResult morph(Player player, DisguiseInfo disguiseInfo, @Nullable Entity targetEntity);

    /**
     * 更新某个伪装的状态
     *
     * @param player 玩家
     * @param state 和玩家对应的{@link DisguiseState}
     * @return 操作是否成功
     */
    public abstract boolean updateDisguise(Player player, DisguiseState state);

    /**
     * 取消某个玩家的伪装
     * @param player 目标玩家
     * @return 操作是否成功
     */
    public boolean unMorph(Player player, DisguiseState state)
    {
        var disguise = state.getDisguise();

        return disguise.removeDisguise(player);
    }

    @Resolved
    private MorphManager morphs;

    protected MorphManager getMorphManager()
    {
        return morphs;
    }

    /**
     * 获取某一实体的伪装
     *
     * @param info 伪装信息
     * @param target 目标实体
     * @return 一个DisguiseResult，其isCopy永远为true。
     */
    @NotNull
    protected DisguiseResult getCopy(DisguiseInfo info, @Nullable Entity target)
    {
        if (target == null) return DisguiseResult.fail();

        boolean shouldClone = false;

        Disguise ourDisguise = null;
        Disguise theirDisguise = null;
        DisguiseState state = null;

        if (DisguiseAPI.isDisguised(target))
        {
            theirDisguise = DisguiseAPI.getDisguise(target);

            //如果玩家已伪装，则检查其目标伪装和我们想要的是否一致
            if (target instanceof Player targetPlayer)
            {
                state = morphs.getDisguiseStateFor(targetPlayer);

                if (state != null)
                {
                    var key = state.getDisguiseIdentifier();

                    //ID不一样则返回失败
                    if (!key.equals(info.getIdentifier())) return DisguiseResult.fail();
                }
            }

            shouldClone = canCopyDisguise(info, target, state, theirDisguise);
        }

        ourDisguise = shouldClone
                ? DisguiseAPI.getDisguise(target).clone()
                : canConstruct(info, target, state) ? DisguiseAPI.constructDisguise(target) : null;

        return ourDisguise == null
                ? DisguiseResult.fail()
                : DisguiseResult.success(ourDisguise, true);
    }

    /**
     * 如果不能复制，那么我们是否可以构建某个实体的伪装?
     *
     * @param info {@link DisguiseInfo}
     * @param targetEntity 目标实体
     * @param theirState 他们的{@link DisguiseState}，为null则代表他们不是玩家或没有通过MorphPlugin伪装
     * @return 是否允许此操作
     */
    protected abstract boolean canConstruct(DisguiseInfo info, Entity targetEntity,
                                            @Nullable DisguiseState theirState);

    /**
     * 是否可以复制某个实体的伪装?
     *
     * @param info {@link DisguiseInfo}
     * @param targetEntity 目标实体
     * @param theirDisguise 他们目前应用的伪装
     * @param theirState 他们的{@link DisguiseState}，为null则代表他们不是玩家或没有通过MorphPlugin伪装
     * @return 是否允许此操作
     */
    protected abstract boolean canCopyDisguise(DisguiseInfo info, Entity targetEntity,
                                               @Nullable DisguiseState theirState, @NotNull Disguise theirDisguise);

    /**
     * 伪装后要做的事
     * @param state {@link DisguiseState}
     * @param targetEntity 目标实体
     */
    public void postConstructDisguise(DisguiseState state, @Nullable Entity targetEntity)
    {
    }

    /**
     * 获取某个伪装的显示名称
     *
     * @param disguiseIdentifier 伪装ID
     * @return 显示名称
     */
    public abstract Component getDisplayName(String disguiseIdentifier);
}
