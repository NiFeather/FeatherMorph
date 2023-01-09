package xiamomc.morph.abilities.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.EffectMorphAbility;

public class SmallJumpBoostAbility extends EffectMorphAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.HAS_SMALL_JUMP_BOOST;
    }

    private final PotionEffect jumpBoostEffectSmall = new PotionEffect(PotionEffectType.JUMP, 11, 0, true, false);

    @Override
    protected PotionEffect getEffect()
    {
        return jumpBoostEffectSmall;
    }
}
