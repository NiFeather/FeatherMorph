package xiamomc.morph.abilities.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.EffectMorphAbility;

public class FireResistanceAbility extends EffectMorphAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.HAS_FIRE_RESISTANCE;
    }

    private final PotionEffect fireResistance = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20, 0, true, false);

    @Override
    protected PotionEffect getEffect()
    {
        return fireResistance;
    }
}
