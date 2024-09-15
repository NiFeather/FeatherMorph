package xiamomc.morph.providers.disguise;

import com.ticxo.modelengine.api.ModelEngineAPI;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.backends.DisguiseBackend;
import xiamomc.morph.backends.DisguiseWrapper;
import xiamomc.morph.backends.modelengine.MEBackend;
import xiamomc.morph.misc.DisguiseMeta;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.DisguiseTypes;
import xiamomc.morph.network.commands.S2C.AbstractS2CCommand;
import xiamomc.morph.providers.animation.AnimationProvider;
import xiamomc.morph.providers.animation.provider.FallbackAnimationProvider;
import xiamomc.morph.providers.animation.provider.VanillaAnimationProvider;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

import java.util.List;

public class ModelEngineProvider extends DisguiseProvider
{
    /**
     * 获取此DisguiseProvider的命名空间，此命名空间将被用于判断某个伪装是否属于此Provider
     *
     * @return 此DisguiseProvider的命名空间
     */
    @Override
    public @NotNull String getNameSpace()
    {
        return DisguiseTypes.MODEL_ENGINE.getNameSpace();
    }

    private final AnimationProvider animationProvider = new FallbackAnimationProvider();

    /**
     * 获取此DisguiseProvider的动画提供器
     */
    @Override
    public AnimationProvider getAnimationProvider()
    {
        return animationProvider;
    }

    @Override
    public boolean allowSwitchingWithoutUndisguise(DisguiseProvider provider, DisguiseMeta meta)
    {
        return false;
    }

    @Override
    public @NotNull DisguiseBackend<?, ?> getPreferredBackend()
    {
        var backend = getMorphManager().getBackend(MEBackend.identifier);
        if (backend == null)
            throw new NullDependencyException("Excepted model engine backend to appear, but it doesn't");

        return backend;
    }

    /**
     * 某个伪装ID是否已知
     *
     * @param rawIdentifier 伪装ID
     * @return 此ID是否已知
     */
    @Override
    public boolean isValid(String rawIdentifier)
    {
        if (DisguiseTypes.fromId(rawIdentifier) != DisguiseTypes.MODEL_ENGINE) return false;

        var modelId = DisguiseTypes.MODEL_ENGINE.toStrippedId(rawIdentifier);

        var registry = ModelEngineAPI.getAPI().getModelRegistry();
        return registry.get(modelId) != registry.getDefault();
    }

    /**
     * Gets all available disguise identifiers for this provider
     *
     * @return A list containing available disguise identifiers for this provider
     * @apiNote The returned values may not contain the namespace of this provider.<br/>
     * For example: `minecraft:ghast` should be `ghast`
     */
    @Override
    public List<String> getAllAvailableDisguises()
    {
        return new ObjectArrayList<>(ModelEngineAPI.getAPI().getModelRegistry().getKeys());
    }

    /**
     * 为目标玩家构建一个用于伪装的 {@link DisguiseWrapper}
     *
     * @param player       目标玩家
     * @param disguiseMeta 伪装ID
     * @param targetEntity 玩家的目标实体(如果有), 可用来判断是否要复制伪装
     */
    @Override
    public @NotNull DisguiseResult makeWrapper(Player player, DisguiseMeta disguiseMeta, @Nullable Entity targetEntity)
    {
        var backend = getMorphManager().getBackend(MEBackend.identifier);
        if (!(backend instanceof MEBackend meBackend))
        {
            logger.warn("Model Engine backend isn't ready yet!");
            return DisguiseResult.FAIL;
        }

        var wrapper = meBackend.createInstance(DisguiseTypes.MODEL_ENGINE.toStrippedId(disguiseMeta.getIdentifier()));

        if (wrapper == null)
            return DisguiseResult.fail();

        return DisguiseResult.success(wrapper);
    }

    /**
     * 更新某个伪装的状态
     *
     * @param player 玩家
     * @param state  和玩家对应的{@link DisguiseState}
     * @return 操作是否成功
     */
    @Override
    public boolean updateDisguise(Player player, DisguiseState state)
    {
        return true;
    }

    /**
     * 获取某个伪装的初始化指令
     *
     * @param state 目标伪装
     * @return 要对客户端发送的指令列表
     */
    @Override
    public @NotNull List<AbstractS2CCommand<?>> getInitialSyncCommands(DisguiseState state)
    {
        return List.of();
    }

    /**
     * 我们是否可以通过给定的{@link DisguiseMeta}来从某个实体构建伪装?
     *
     * @param info         {@link DisguiseMeta}
     * @param targetEntity 目标实体
     * @param theirState   他们的{@link DisguiseState}，为null则代表他们不是玩家或没有通过MorphPlugin伪装
     * @return 是否允许此操作，如果theirState不为null则优先检查theirState是否和传入的info相匹配
     */
    @Override
    public boolean canConstruct(DisguiseMeta info, Entity targetEntity, @Nullable DisguiseState theirState)
    {
        return false;
    }

    /**
     * 是否可以克隆某个实体现有的伪装?
     *
     * @param info          {@link DisguiseMeta}
     * @param targetEntity  目标实体
     * @param theirState    他们的{@link DisguiseState}，为null则代表他们不是玩家或没有通过MorphPlugin伪装
     * @param theirDisguise 他们目前应用的伪装
     * @return 是否允许此操作
     */
    @Override
    protected boolean canCloneDisguise(DisguiseMeta info, Entity targetEntity, @NotNull DisguiseState theirState, @NotNull DisguiseWrapper<?> theirDisguise)
    {
        return false;
    }

    /**
     * 获取某个伪装的显示名称
     *
     * @param disguiseIdentifier 伪装ID
     * @param locale             显示名称的目标语言
     * @return 显示名称
     */
    @Override
    public Component getDisplayName(String disguiseIdentifier, @Nullable String locale)
    {
        return Component.text(disguiseIdentifier);
    }
}
