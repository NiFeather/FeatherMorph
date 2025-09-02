package xyz.nifeather.morph.abilities.impl.onAttack;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.abilities.impl.OnAttackAbility;
import xyz.nifeather.morph.abilities.options.PotionEffectOption;
import xyz.nifeather.morph.api.morphs.abilities.AbilityNames;

public class PotionOnAttackAbility extends OnAttackAbility<PotionEffectOption>
{
    @Override
    public @NotNull ISkillAbilityOptionHandler<PotionEffectOption> optionHandler()
    {
        return PotionEffectOption.OPTION_HANDLER;
    }

    /**
     * 获取此被动技能的ID
     *
     * @return {@link NamespacedKey}
     */
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityNames.POTION_ON_ATTACK;
    }

    @Resolved
    private MorphManager manager;

    @Override
    protected boolean requireValidOption()
    {
        return true;
    }

    @Override
    protected void onAttack(LivingEntity hurt, Player source)
    {
        var state = manager.getDisguiseStateFor(source);
        if (state == null) return;

        var option = this.getOptionFor(state);

        if (option == null || !option.isValid()) return;

        var effectType = option.effectType;
        if (effectType == null) return;

        var effect = new PotionEffect(effectType, option.duration, option.amplifier, false);
        hurt.addPotionEffect(effect);
    }
}
