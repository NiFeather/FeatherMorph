package xiamomc.morph.providers;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
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
     * @return 一个DisguiseResult
     * @apiNote 暂时不会判断目标实体的伪装的ID
     */
    @NotNull
    protected DisguiseResult getCopy(DisguiseInfo info, @Nullable Entity target)
    {
        if (target == null) return DisguiseResult.fail();

        boolean shouldCopy = false;
        Disguise disguise;

        if (DisguiseAPI.isDisguised(target))
        {
            var disg = DisguiseAPI.getDisguise(target);
            assert disg != null;

            var type = info.getEntityType();

            //检查伪装ID
            if (target instanceof Player targetPlayer)
            {
                //查询此玩家的伪装是否是LD自定义伪装
                var state = morphs.getDisguiseStateFor(targetPlayer);

                if (state != null)
                {
                    var key = state.getDisguiseIdentifier();

                    //ID不一样则返回null
                    if (!key.equals(info.getIdentifier())) return DisguiseResult.fail();
                }
            }

            shouldCopy = (info.isPlayerDisguise()
                    ? disg.isPlayerDisguise() && ((PlayerDisguise) disg).getName().equals(info.playerDisguiseTargetName)
                    : disg.getType().getEntityType().equals(type));
        }

        disguise = shouldCopy
                ? DisguiseAPI.getDisguise(target)
                : DisguiseAPI.constructDisguise(target);

        return DisguiseResult.success(disguise, shouldCopy) ;
    }

    /**
     * 伪装后要做的事
     * @param state {@link DisguiseState}
     * @param targetEntity 目标实体
     */
    public void postConstructDisguise(DisguiseState state, @Nullable Entity targetEntity)
    {
    }
}
