package xiamomc.morph.abilities;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.storage.skill.ISkillOption;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public abstract class MorphAbility<T extends ISkillOption> extends MorphPluginObject implements IMorphAbility<T>
{
    protected final List<Player> appliedPlayers = new ObjectArrayList<>();

    protected boolean requireValidOption()
    {
        return false;
    }

    private boolean optionValid = true;
    public boolean optionValid()
    {
        return optionValid;
    }

    @Override
    public boolean applyToPlayer(Player player, DisguiseState state)
    {
        synchronized (appliedPlayers)
        {
            if (appliedPlayers.contains(player)) return true;

            appliedPlayers.add(player);
        }

        if (!requireValidOption()) return true;

        var option = this.getOptionFor(state);
        if (option != null && option.isValid()) return true;

        logger.error("Disguise '%s' does not have a valid configuration for ability %s, not processing...".formatted(state.getDisguiseIdentifier(), getIdentifier()));
        synchronized (appliedPlayers)
        {
            appliedPlayers.remove(player);
            optionValid = false;
        }

        return false;
    }

    @Override
    public boolean revokeFromPlayer(Player player, DisguiseState state)
    {
        synchronized (appliedPlayers)
        {
            appliedPlayers.remove(player);
        }

        return true;
    }

    @Override
    public List<Player> getAppliedPlayers()
    {
        return appliedPlayers;
    }

    @NotNull
    protected abstract T createOption();

    private final T option = createOption();

    @Override
    @NotNull
    public T getDefaultOption()
    {
        return option;
    }

    /**
     * 获取和目标{@link DisguiseState}对应的技能配置
     * @param state {@link DisguiseState}
     * @return 和此 {@link DisguiseState}对应的技能配置
     */
    @Nullable
    protected T getOptionFor(DisguiseState state)
    {
        return getOr(
                () -> optionMap.get(state.getDisguiseIdentifier()),
                Objects::nonNull,
                () -> optionMap.get(state.skillLookupIdentifier())
        );
    }

    public interface Returner<R>
    {
        R apply();
    }

    /**
     * 根据情况返回特定的值
     *
     * @param value 首选值
     * @param p 判断条件
     * @param fallbackValue 后备值
     * @return 若value满足判断条件，则返回value，否则返回fallbackValue
     */
    protected <R> R getOr(Returner<R> value, Predicate<R> p, Returner<R> fallbackValue)
    {
        var val = value.apply();

        if (p.test(val))
            return val;
        else
            return fallbackValue.apply();
    }

    protected final Map<String, T> optionMap = new Object2ObjectOpenHashMap<>();

    @Override
    public boolean setOption(@NotNull String disguiseIdentifier, @Nullable T option)
    {
        if (option == null)
            return false;

        optionMap.put(disguiseIdentifier, option);

        return true;
    }

    @Override
    public void clearOptions()
    {
        optionMap.clear();
    }
}
