package xyz.nifeather.morph.abilities.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.AbilityType;
import xyz.nifeather.morph.abilities.EffectMorphAbility;

public class SmallJumpBoostAbility extends EffectMorphAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.HAS_SMALL_JUMP_BOOST;
    }

    private final PotionEffect jumpBoostEffectSmall = new PotionEffect(PotionEffectType.JUMP_BOOST, 11, 0, true, false);

    @Override
    protected PotionEffect getEffect()
    {
        return jumpBoostEffectSmall;
    }
}
