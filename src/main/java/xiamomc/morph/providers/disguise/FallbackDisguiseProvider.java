package xiamomc.morph.providers.disguise;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.backends.DisguiseWrapper;
import xiamomc.morph.misc.DisguiseMeta;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.providers.animation.AnimationProvider;
import xiamomc.morph.providers.animation.provider.FallbackAnimationProvider;

import java.util.List;

public class FallbackDisguiseProvider extends DefaultDisguiseProvider
{
    @Override
    public @NotNull String getNameSpace()
    {
        return "fallback";
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
    public boolean isValid(String rawIdentifier)
    {
        return false;
    }

    @Override
    public List<String> getAllAvailableDisguises()
    {
        return List.of();
    }

    @Override
    public @NotNull DisguiseResult makeWrapper(Player player, DisguiseMeta disguiseMeta, @Nullable Entity targetEntity)
    {
        return DisguiseResult.fail();
    }

    @Override
    public boolean canConstruct(DisguiseMeta info, Entity targetEntity, @Nullable DisguiseState theirState)
    {
        return false;
    }

    /**
     * 我们是否可以克隆目标实体/玩家的伪装？
     *
     * @param info         {@link DisguiseMeta}
     * @param targetEntity 目标实体
     * @param theirState   他们的{@link DisguiseState}，如果有
     * @return 是否允许克隆他们的装备进行显示
     */
    @Override
    public boolean canCloneEquipment(DisguiseMeta info, Entity targetEntity, DisguiseState theirState)
    {
        return false;
    }

    @Override
    protected boolean canCloneDisguise(DisguiseMeta info, Entity targetEntity, @NotNull DisguiseState theirState, @NotNull DisguiseWrapper<?> theirDisguise)
    {
        return false;
    }

    @Override
    public Component getDisplayName(String disguiseIdentifier, String locale)
    {
        return Component.text("???");
    }
}
