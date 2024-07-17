package xiamomc.morph.providers;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.DisguiseBackend;
import xiamomc.morph.backends.DisguiseWrapper;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.misc.DisguiseMeta;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.network.commands.S2C.AbstractS2CCommand;
import xiamomc.morph.utilities.NbtUtils;
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
     * 是否允许在切换到其他伪装时可以不调用此Provider的Unmorph
     * @return
     */
    public abstract boolean allowSwitchingWithoutUndisguise(DisguiseProvider provider, DisguiseMeta meta);

    /**
     * 某个伪装ID是否已知
     * @param rawIdentifier 伪装ID
     * @return 此ID是否已知
     */
    public abstract boolean isValid(String rawIdentifier);

    /**
     * Gets all available disguise identifiers for this provider
     * @return A list containing available disguise identifiers for this provider
     * @apiNote The returned values may not contain the namespace of this provider.<br/>
     *          For example: `minecraft:ghast` should be `ghast`
     */
    public abstract List<String> getAllAvailableDisguises();

    /**
     * 为目标玩家构建一个用于伪装的 {@link DisguiseWrapper}
     *
     * @param player 目标玩家
     * @param disguiseMeta 伪装ID
     * @param targetEntity 玩家的目标实体(如果有), 可用来判断是否要复制伪装
     */
    @NotNull
    public abstract DisguiseResult makeWrapper(Player player, DisguiseMeta disguiseMeta, @Nullable Entity targetEntity);

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

    /**
     * 获取此伪装的初始NBT（如果有），如果有目标实体则尝试复制目标实体的NBT
     * @param state
     * @param targetEntity
     * @param enableCulling 是否要隐藏一些信息防止被客户端获取？
     * @return
     */
    @Nullable
    public CompoundTag getInitialNbtCompound(DisguiseState state, Entity targetEntity, boolean enableCulling)
    {
        if (targetEntity instanceof CraftLivingEntity
            && canConstruct(getMorphManager().getDisguiseMeta(state.getDisguiseIdentifier()), targetEntity, null))
        {
            var rawCompound = NbtUtils.getRawTagCompound(targetEntity);

            return enableCulling ? cullNBT(rawCompound) : rawCompound;
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
            configManager.bind(String.class, blackListTags, ConfigOption.BLACKLIST_TAGS);
            configManager.bind(String.class, blackListPatterns, ConfigOption.BLACKLIST_PATTERNS);

            bindableInitialized = true;
        }
    }

    public static CompoundTag cullNBT(CompoundTag compound)
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
     * Gets the backend used by this provider
     * @return A disguise backend.
     * @apiNote The return value SHOULD NOT CHANGE, or will lead to undefined behaviors...
     */
    @NotNull
    public abstract DisguiseBackend<?, ?> getPreferredBackend();

    /**
     * Resets a player's some attributes from a disguise but not un-disguising them.
     * Used for keeping disguise appearance while switching (if this provider allow switching to that disguise without un-disguise)
     *
     * @param state The {@link DisguiseState} to reset
     */
    public void resetDisguise(DisguiseState state)
    {
    }

    /**
     * 取消某个玩家的伪装
     * @param player 目标玩家
     * @return 操作是否成功
     */
    public boolean unMorph(Player player, DisguiseState state)
    {
        return getPreferredBackend().unDisguise(player);
    }

    @Resolved
    private MorphManager morphs;

    protected MorphManager getMorphManager()
    {
        return morphs;
    }

    /**
     * 从某一实体构建伪装
     *
     * @param info 伪装信息
     * @param target 目标实体
     * @return 一个包含伪装Wrapper的 {@link DisguiseResult}, 失败时返回 {@link DisguiseResult#FAIL}
     */
    @NotNull
    protected DisguiseResult constructFromEntity(DisguiseMeta info, @Nullable Entity target)
    {
        if (target == null) return DisguiseResult.fail();

        boolean allowClone = false;

        var backend = getPreferredBackend();

        DisguiseWrapper<?> ourDisguise;
        DisguiseWrapper<?> theirDisguise = backend.getWrapper(target);
        DisguiseState theirState = morphs.getDisguiseStateFor(target);

        if (theirState != null && theirDisguise != null)
        {
            var key = theirState.getDisguiseIdentifier();

            //ID不一样则返回失败
            if (!key.equals(info.getIdentifier())) return DisguiseResult.fail();

            allowClone = canCloneDisguise(info, target, theirState, theirDisguise);
        }

        ourDisguise = allowClone
                ? theirDisguise.clone()
                : canConstruct(info, target, theirState) ? backend.createInstance(target) : null;

        return ourDisguise == null
                ? DisguiseResult.fail()
                : DisguiseResult.success(ourDisguise, true);
    }

    /**
     * 我们是否可以通过给定的{@link DisguiseMeta}来从某个实体构建伪装?
     *
     * @param info {@link DisguiseMeta}
     * @param targetEntity 目标实体
     * @param theirState 他们的{@link DisguiseState}，为null则代表他们不是玩家或没有通过MorphPlugin伪装
     * @return 是否允许此操作，如果theirState不为null则优先检查theirState是否和传入的info相匹配
     */
    public abstract boolean canConstruct(DisguiseMeta info, Entity targetEntity,
                                         @Nullable DisguiseState theirState);

    /**
     * 是否可以克隆某个实体现有的伪装?
     *
     * @param info {@link DisguiseMeta}
     * @param targetEntity 目标实体
     * @param theirDisguise 他们目前应用的伪装
     * @param theirState 他们的{@link DisguiseState}，为null则代表他们不是玩家或没有通过MorphPlugin伪装
     * @return 是否允许此操作
     */
    protected abstract boolean canCloneDisguise(DisguiseMeta info, Entity targetEntity,
                                                @NotNull DisguiseState theirState, @NotNull DisguiseWrapper<?> theirDisguise);

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
