package xiamomc.morph.abilities.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.EffectMorphAbility;

public class BreatheUnderWaterAbility extends EffectMorphAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.CAN_BREATHE_UNDER_WATER;
    }

    private final PotionEffect conduitEffect = new PotionEffect(PotionEffectType.CONDUIT_POWER, 20, 0);

    @Override
    protected PotionEffect getEffect()
    {
        return conduitEffect;
    }
}
