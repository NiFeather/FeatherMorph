package xyz.nifeather.morph.abilities;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class MorphAbility<T extends ISkillAbilityOption> extends MorphPluginObject implements IAbility<T>
{
    private final List<UUID> appliedPlayers = Collections.synchronizedList(new ObjectArrayList<>());

    protected boolean requireValidOption()
    {
        return false;
    }

    public boolean isPlayerApplied(Player player)
    {
        return this.isPlayerApplied(player.getUniqueId());
    }

    public boolean isPlayerApplied(UUID playerUUID)
    {
        return this.appliedPlayers.stream().anyMatch(uuid -> uuid.equals(playerUUID));
    }

    // WTF is this doing?
    @Override
    public boolean applyToPlayer(Player player, DisguiseState state)
    {
        synchronized (appliedPlayers)
        {
            if (appliedPlayers.stream().anyMatch(uuid -> uuid.equals(player.getUniqueId()))) return true;

            appliedPlayers.add(player.getUniqueId());
        }

        if (!requireValidOption()) return true;

        var option = this.getOptionFor(state);
        if (option != null && option.isValid()) return true;

        logger.error("Disguise '%s' does not have a valid configuration for ability %s, not processing...".formatted(state.getDisguiseIdentifier(), getIdentifier()));
        synchronized (appliedPlayers)
        {
            appliedPlayers.remove(player.getUniqueId());
        }

        return false;
    }

    @Override
    public boolean revokeFromPlayer(Player player, DisguiseState state)
    {
        synchronized (appliedPlayers)
        {
            appliedPlayers.remove(player.getUniqueId());
        }

        return true;
    }

    @Override
    @Unmodifiable
    public List<UUID> getAppliedPlayers()
    {
        return new ObjectArrayList<>(appliedPlayers);
    }

    /**
     * 获取和目标{@link DisguiseState}对应的技能配置
     * @param state {@link DisguiseState}
     * @return 和此 {@link DisguiseState}对应的技能配置
     */
    @Nullable
    protected T getOptionFor(DisguiseState state)
    {
        return state.getAbilityUpdater()
                .lookupAbilityConfig(this.getIdentifier().asString(), optionHandler().getOptionClass());
    }

    /**
     * 根据情况返回特定的值
     *
     * @param value 首选值
     * @param p 判断条件
     * @param fallbackValue 后备值
     * @return 若value满足判断条件，则返回value，否则返回fallbackValue
     */
    protected <R> R getOr(Supplier<R> value, Predicate<R> p, Supplier<R> fallbackValue)
    {
        var val = value.get();

        if (p.test(val))
            return val;
        else
            return fallbackValue.get();
    }
}
