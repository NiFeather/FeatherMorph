package xiamomc.morph.abilities.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.EffectMorphAbility;

public class SpeedBoostAbility extends EffectMorphAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.HAS_SPEED_BOOST;
    }

    private final PotionEffect speedEffect = new PotionEffect(PotionEffectType.SPEED, 11, 2, true, false);

    @Override
    protected PotionEffect getEffect()
    {
        return speedEffect;
    }
}
