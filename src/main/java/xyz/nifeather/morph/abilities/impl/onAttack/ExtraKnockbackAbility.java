package xyz.nifeather.morph.abilities.impl.onAttack;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.abilities.impl.OnAttackAbility;
import xyz.nifeather.morph.abilities.options.ExtraKnockbackOption;
import xyz.nifeather.morph.api.morphs.abilities.AbilityNames;

import java.util.Optional;

public class ExtraKnockbackAbility extends OnAttackAbility<ExtraKnockbackOption>
{
    @Override
    public @NotNull ISkillAbilityOptionHandler<ExtraKnockbackOption> optionHandler()
    {
        return ExtraKnockbackOption.OPTION_HANDLER;
    }

    /**
     * 获取此被动技能的ID
     *
     * @return {@link NamespacedKey}
     */
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityNames.EXTRA_KNOCKBACK;
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphManager manager;

    @Override
    protected void onAttack(org.bukkit.entity.LivingEntity damaged, Player player)
    {
        var state = manager.getDisguiseStateFor(player);

        assert state != null;
        var option = this.getOptionFor(state);

        if (option == null) option = defaultOption;

        //var yDelta = 0.745584025D;

        double knockbackResistance = Optional.ofNullable(damaged.getAttribute(Attribute.KNOCKBACK_RESISTANCE))
                .map(AttributeInstance::getValue)
                .orElse(0d);

        double knockMultiplier = Math.max(0, 1d - knockbackResistance);

        var yDelta = option.yMotion * knockMultiplier;
        var xDelta = option.xMotion * knockMultiplier;
        var zDelta = option.zMotion * knockMultiplier;

        //workaround: 需要让实体离地再设置Motion，否则不会起效
        var movement = damaged.getVelocity().clone().add(new Vector(xDelta, yDelta, zDelta));
        damaged.teleportAsync(damaged.getLocation().clone().add(0, 0.01d, 0))
                .thenRun(() -> damaged.setVelocity(movement));
    }

    private static final ExtraKnockbackOption defaultOption = ExtraKnockbackOption.from(0, 0.4D, 0);

}
