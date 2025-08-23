package xyz.nifeather.morph.abilities.impl.onAttack;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.abilities.impl.OnAttackAbility;
import xyz.nifeather.morph.abilities.options.ExtraKnockbackOption;
import xyz.nifeather.morph.api.morphs.abilities.AbilityNames;

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
        var nmsDamaged = ((CraftEntity)damaged).getHandle();

        var state = manager.getDisguiseStateFor(player);

        assert state != null;
        var option = this.getOptionFor(state);

        if (option == null) option = defaultOption;

        //var yDelta = 0.745584025D;
        var yDelta = option.yMotion;
        var baseYDelta = 0.345584025D;

        if (nmsDamaged instanceof LivingEntity livingEntity)
        {
            var knockbackResistance = livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
            yDelta *= Math.max(0D, 1D - knockbackResistance);
            baseYDelta *= Math.max(0D, 1D - knockbackResistance);
        }

        yDelta = baseYDelta + yDelta;

        //workaround: 需要让实体离地再设置Motion，否则不会起效
        var movement = nmsDamaged.getDeltaMovement().add(option.xMotion, yDelta, option.zMotion);
        nmsDamaged.setPos(nmsDamaged.position().add(0, 0.01D, 0));
        nmsDamaged.setOnGround(false);
        nmsDamaged.setDeltaMovement(movement);
        nmsDamaged.hasImpulse = true;
    }

    private static final ExtraKnockbackOption defaultOption = ExtraKnockbackOption.from(0, 0.4D, 0);

}
