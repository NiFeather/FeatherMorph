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

    @Override
    public boolean applyToPlayer(Player player, DisguiseState state)
    {
        synchronized (appliedPlayers)
        {
            if (appliedPlayers.contains(player)) return true;

            appliedPlayers.add(player);
        }

        return true;
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

    /**
     * 根据情况返回特定的值
     *
     * @param value 首选值
     * @param p 判断条件
     * @param fallbackValue 后备值
     * @return 若value满足判断条件，则返回value，否则返回fallbackValue
     */
    protected <R> R getOr(R value, Predicate<R> p, R fallbackValue)
    {
        if (p.test(value))
            return value;
        else
            return fallbackValue;
    }

    protected abstract T createOption();

    private final T option = createOption();

    @Override
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
                options.get(state.getDisguiseIdentifier()),
                Objects::nonNull,
                options.get(state.getSkillLookupIdentifier())
        );
    }

    protected final Map<String, T> options = new Object2ObjectOpenHashMap<>();

    @Override
    public boolean setOption(@NotNull String disguiseIdentifier, @Nullable T option)
    {
        if (option == null)
            return false;

        options.put(disguiseIdentifier, option);

        return true;
    }

    @Override
    public void clearOptions()
    {
        options.clear();
    }
}
