package xiamomc.morph.abilities;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import xiamomc.morph.abilities.impl.NoOpOptionAbility;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.pluginbase.Annotations.Initializer;

public abstract class EffectMorphAbility extends NoOpOptionAbility
{
    protected abstract PotionEffect getEffect();

    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        if (plugin.getCurrentTick() % this.refreshInterval == 0)
            player.addPotionEffect(getEffect());

        return true;
    }

    @Override
    public boolean applyToPlayer(Player player, DisguiseState state)
    {
        if (super.applyToPlayer(player, state))
        {
            player.addPotionEffect(getEffect());
            return true;
        }
        else
            return false;
    }

    @Initializer
    private void load()
    {
        var effect = this.getEffect();
        var interval = this.getRefreshInterval();

        interval = interval == -1
                ? Math.max(effect.getDuration() - 1, effect.getDuration() - 20)
                : interval;

        interval = Math.max(0, interval);

        this.refreshInterval = interval;
    }

    private int refreshInterval = -1;

    protected int getRefreshInterval()
    {
        return -1;
    }
}
