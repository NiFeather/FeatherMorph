package xiamomc.morph.abilities;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import xiamomc.morph.misc.DisguiseState;

public abstract class EffectMorphAbility extends MorphAbility
{
    protected abstract PotionEffect getEffect();

    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        player.addPotionEffect(getEffect());

        return true;
    }
}
