package xyz.nifeather.morph.abilities.impl.potion;

import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.EffectMorphAbility;
import xyz.nifeather.morph.api.morphs.abilities.AbilityNames;

public class NightVisionAbility extends EffectMorphAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityNames.ALWAYS_NIGHT_VISION;
    }

    private final PotionEffect nightVisionEffect = new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0, true, false);

    @Override
    protected PotionEffect getEffect()
    {
        return nightVisionEffect;
    }

    @Override
    protected int getRefreshInterval()
    {
        return 40;
    }
}
