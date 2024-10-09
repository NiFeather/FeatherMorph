package xyz.nifeather.morph.abilities.impl.potion;

import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.AbilityType;
import xyz.nifeather.morph.abilities.EffectMorphAbility;

public class BreatheUnderWaterAbility extends EffectMorphAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.CAN_BREATHE_UNDER_WATER;
    }

    private final PotionEffect conduitEffect = new PotionEffect(PotionEffectType.CONDUIT_POWER, 20, 0, true, false);

    @Override
    protected PotionEffect getEffect()
    {
        return conduitEffect;
    }
}
