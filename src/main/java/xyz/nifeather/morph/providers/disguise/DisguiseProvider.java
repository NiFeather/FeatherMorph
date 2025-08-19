package xyz.nifeather.morph.providers.disguise;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.backends.DisguiseBackend;
import xyz.nifeather.morph.backends.DisguiseWrapper;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.network.commands.S2C.AbstractS2CCommand;
import xyz.nifeather.morph.providers.animation.AnimationProvider;

import java.util.List;

/**
 * 负责提供伪装及其相关功能的组件。
 * 目前包括：伪装功能和伪装动作
 */
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
     * 获取此DisguiseProvider的动画提供器
     */
    public abstract AnimationProvider getAnimationProvider();

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
     * Setup properties for the given disguise.<br>
     * Please note that this will apply before player inputs, means that players may override changes made by the provider
     * @param state The disguise to setup
     * @param targetEntity Player's targeted entity, NULL if none
     */
    public void setupProperties(DisguiseState state, @Nullable Entity targetEntity)
    {
        var matchingProperty = DisguiseProperties.INSTANCE.get(state.getEntityType());
        matchingProperty.setupProperties(state, targetEntity);
    }

    /**
     * 获取某个伪装的初始化指令
     * @param state 目标伪装
     * @return 要对客户端发送的指令列表
     */
    @NotNull
    public abstract List<AbstractS2CCommand<?>> getInitialSyncCommands(DisguiseState state);

    public boolean validForClient(DisguiseState state)
    {
        return false;
    }

    /**
     * Gets the backend used by this provider
     * @return A disguise backend.
     * @apiNote The return value SHOULD NOT CHANGE, or will lead to undefined behaviors...
     */
    @NotNull
    public abstract DisguiseBackend<?, ?> getPreferredBackend();

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
     * 我们是否可以克隆目标实体/玩家的伪装？
     *
     * @param info {@link DisguiseMeta}
     * @param targetEntity 目标实体
     * @param theirState 他们的{@link DisguiseState}，如果有
     * @return 是否允许克隆他们的装备进行显示
     */
    public abstract boolean canCloneEquipment(DisguiseMeta info, Entity targetEntity, DisguiseState theirState);

    /**
     * 伪装后要做的事
     * @param state {@link DisguiseState}
     * @param targetEntity 目标实体
     */
    public void postBuildDisguise(DisguiseState state, @Nullable Entity targetEntity)
    {
    }

    /**
     * Execute when a disguise is applied
     */
    public void onDisguiseApply(DisguiseState state)
    {
    }

    /**
     * Execute when a player joined with disguise made by this provider,
     * Called at {@link DisguiseState#onPlayerJoin()}
     */
    public void onPlayerJoinWithDisguise(DisguiseState state)
    {
    }

    /**
     * Execute when a player quit with disguise made by this provider,
     * Called at {@link DisguiseState#onPlayerQuit()}
     */
    public void onPlayerQuitWithDisguise(DisguiseState state)
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
