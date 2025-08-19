package xyz.nifeather.morph.abilities.impl.onAttack;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.abilities.impl.OnAttackAbility;
import xyz.nifeather.morph.abilities.options.PotionEffectOption;
import xyz.nifeather.morph.api.morphs.abilities.AbilityNames;

public class PotionOnAttackAbility extends OnAttackAbility<PotionEffectOption>
{
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

    @Override
    protected @NotNull PotionEffectOption createOption()
    {
        return new PotionEffectOption();
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

        var effectType = PotionEffectType.getByName(option.effectId.replace("minecraft:", ""));
        if (effectType == null) return;

        var effect = new PotionEffect(effectType, option.duration, option.amplifier, false);
        hurt.addPotionEffect(effect);
    }
}
