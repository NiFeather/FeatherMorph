package xiamomc.morph.abilities.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.EffectMorphAbility;

public class FeatherFallingAbility extends EffectMorphAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.HAS_FEATHER_FALLING;
    }

    private final PotionEffect featherFallingEffect = new PotionEffect(PotionEffectType.SLOW_FALLING, 5, 0, true, false);

    @Override
    protected PotionEffect getEffect()
    {
        return featherFallingEffect;
    }
}
