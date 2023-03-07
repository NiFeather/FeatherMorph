package xiamomc.morph.abilities.impl.potion;

import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.EffectMorphAbility;

public class NightVisionAbility extends EffectMorphAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.ALWAYS_NIGHT_VISION;
    }

    private final PotionEffect nightVisionEffect = new PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0);

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
