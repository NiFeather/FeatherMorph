package xiamomc.morph.abilities.impl.potion;

import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.EffectMorphAbility;

public class JumpBoostAbility extends EffectMorphAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.HAS_JUMP_BOOST;
    }

    private final PotionEffect jumpBoostEffect = new PotionEffect(PotionEffectType.JUMP_BOOST, 5, 1, true, false);

    @Override
    protected PotionEffect getEffect()
    {
        return jumpBoostEffect;
    }
}
