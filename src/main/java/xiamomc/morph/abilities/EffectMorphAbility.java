package xiamomc.morph.abilities;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import xiamomc.morph.abilities.impl.NoOpOptionAbility;
import xiamomc.morph.misc.DisguiseState;

public abstract class EffectMorphAbility extends NoOpOptionAbility
{
    protected abstract PotionEffect getEffect();

    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        player.addPotionEffect(getEffect());

        return true;
    }
}
