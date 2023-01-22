package xiamomc.morph.providers;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.*;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.misc.DisguiseInfo;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.utilities.NbtUtils;
import xiamomc.morph.network.commands.S2C.AbstractS2CCommand;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.BindableList;

import java.util.List;
import java.util.regex.Pattern;

public abstract class DisguiseProvider extends MorphPluginObject
{
    /**
     * 获取此DisguiseProvider的命名空间，此命名空间将被用于判断某个伪装是否属于此Provider
     *
     * @return 此DisguiseProvider的命名空间
     */
    @NotNull
    public abstract String getNameSpace();

    /**
     * Gets all available disguise identifiers for this provider
     * @return A list containing available disguise identifiers for this provider
     */
    public abstract List<String> getAllAvailableDisguises();

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
     * 获取某个伪装的初始化指令
     * @param state 目标伪装
     * @return 要对客户端发送的指令列表
     */
    @NotNull
    public abstract List<AbstractS2CCommand<?>> getInitialSyncCommands(DisguiseState state);

    /**
     * 获取某个伪装的客户端预览ID
     * @param state 目标伪装
     * @return ID
     */
    public String getSelfViewIdentifier(DisguiseState state)
    {
        return state.getDisguiseIdentifier();
    }

    public boolean validForClient(DisguiseState state)
    {
        return false;
    }

    @Nullable
    public CompoundTag getNbtCompound(DisguiseState state, Entity targetEntity)
    {
        if (targetEntity instanceof CraftLivingEntity
            && canConstruct(getMorphManager().getDisguiseInfo(state.getDisguiseIdentifier()), targetEntity, null))
        {
            var rawCompound = NbtUtils.getRawTagCompound(targetEntity);

            return cullNBT(rawCompound);
        }

        return null;
    }

    private static final BindableList<String> blackListTags = new BindableList<>();
    private static final BindableList<String> blackListPatterns = new BindableList<>();
    private static boolean bindableInitialized;

    @Initializer
    private void load(MorphConfigManager configManager)
    {
        if (!bindableInitialized)
        {
            configManager.bind(blackListTags, ConfigOption.BLACKLIST_TAGS);
            configManager.bind(blackListPatterns, ConfigOption.BLACKLIST_PATTERNS);

            bindableInitialized = true;
        }
    }

    protected CompoundTag cullNBT(CompoundTag compound)
    {
        if (compound == null) return null;

        //compound.r() -> NBTCompound#remove()

        blackListTags.forEach(compound::remove);

        var toRemove = new ObjectArrayList<String>();

        blackListPatterns.forEach(pattern ->
        {
            compound.tags.keySet().forEach(id ->
            {
                if (Pattern.matches(pattern, id))
                    toRemove.add(id);
            });
        });

        toRemove.forEach(compound::remove);
        return compound;
    }

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
    public abstract boolean canConstruct(DisguiseInfo info, Entity targetEntity,
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
     * @param locale 显示名称的目标语言
     * @return 显示名称
     */
    public abstract Component getDisplayName(String disguiseIdentifier, @Nullable String locale);
}
