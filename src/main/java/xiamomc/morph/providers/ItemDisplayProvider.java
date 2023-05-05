package xiamomc.morph.providers;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.backends.DisguiseWrapper;
import xiamomc.morph.backends.libsdisg.LibsBackend;
import xiamomc.morph.misc.DisguiseInfo;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.DisguiseTypes;

import java.util.List;

@ApiStatus.Experimental
public class ItemDisplayProvider extends DefaultDisguiseProvider
{
    /**
     * 获取此DisguiseProvider的命名空间，此命名空间将被用于判断某个伪装是否属于此Provider
     *
     * @return 此DisguiseProvider的命名空间
     */
    @Override
    public @NotNull String getNameSpace()
    {
        return DisguiseTypes.ITEM_DISPLAY.getNameSpace();
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
        return getMaterialFromId(rawIdentifier) != null;
    }

    @Nullable
    private Material getMaterialFromId(String rawIdentifier)
    {
        var strippedId = DisguiseTypes.ITEM_DISPLAY.toStrippedId(rawIdentifier);

        return Material.matchMaterial(strippedId);
    }

    /**
     * Gets all available disguise identifiers for this provider
     *
     * @return A list containing available disguise identifiers for this provider
     */
    @Override
    public List<String> getAllAvailableDisguises()
    {
        return List.of(DisguiseTypes.ITEM_DISPLAY.toId("dragon_head"));
    }

    /**
     * 为某个玩家创建 {@link DisguiseWrapper}
     *
     * @param player       目标玩家
     * @param disguiseInfo 伪装ID
     * @param targetEntity 玩家的目标实体(如果有), 可用来判断是否要复制伪装
     * @return 操作结果
     */
    @Override
    public @NotNull DisguiseResult makeWrapper(Player player, DisguiseInfo disguiseInfo, @Nullable Entity targetEntity)
    {
        var currentBackend = getBackend();

        if (!(currentBackend instanceof LibsBackend backend))
        {
            logger.error("Item Displays only works with the LibsDisguises backend");
            return DisguiseResult.fail();
        }

        var material = getMaterialFromId(disguiseInfo.rawIdentifier);
        if (material == null) return DisguiseResult.fail();

        var wrapper = backend.createItemDisplay(material);

        return DisguiseResult.success(wrapper);
    }

    @Override
    public boolean unMorph(Player player, DisguiseState state)
    {
        return super.unMorph(player, state);
    }

    /**
     * 我们是否可以通过给定的{@link DisguiseInfo}来从某个实体构建伪装?
     *
     * @param info         {@link DisguiseInfo}
     * @param targetEntity 目标实体
     * @param theirState   他们的{@link DisguiseState}，为null则代表他们不是玩家或没有通过MorphPlugin伪装
     * @return 是否允许此操作，如果theirState不为null则优先检查theirState是否和传入的info相匹配
     */
    @Override
    public boolean canConstruct(DisguiseInfo info, Entity targetEntity, @Nullable DisguiseState theirState)
    {
        return false;
    }

    /**
     * 是否可以克隆某个实体现有的伪装?
     *
     * @param info          {@link DisguiseInfo}
     * @param targetEntity  目标实体
     * @param theirState    他们的{@link DisguiseState}，为null则代表他们不是玩家或没有通过MorphPlugin伪装
     * @param theirDisguise 他们目前应用的伪装
     * @return 是否允许此操作
     */
    @Override
    protected boolean canCloneDisguise(DisguiseInfo info, Entity targetEntity, @NotNull DisguiseState theirState, @NotNull DisguiseWrapper<?> theirDisguise)
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
