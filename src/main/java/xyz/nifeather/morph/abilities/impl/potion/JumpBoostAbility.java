package xyz.nifeather.morph.abilities.impl.potion;

import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.EffectMorphAbility;
import xyz.nifeather.morph.api.morphs.abilities.AbilityNames;

public class JumpBoostAbility extends EffectMorphAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityNames.HAS_JUMP_BOOST;
    }

    private final PotionEffect jumpBoostEffect = new PotionEffect(PotionEffectType.JUMP_BOOST, 5, 1, true, false);

    @Override
    protected PotionEffect getEffect()
    {
        return jumpBoostEffect;
    }
}
