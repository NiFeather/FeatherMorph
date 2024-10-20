package xyz.nifeather.morph.abilities.impl.potion;

import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.AbilityType;
import xyz.nifeather.morph.abilities.EffectMorphAbility;

@Deprecated
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
