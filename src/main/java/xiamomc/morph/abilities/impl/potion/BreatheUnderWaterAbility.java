package xiamomc.morph.abilities.impl.potion;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.EffectMorphAbility;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.NmsRecord;

public class BreatheUnderWaterAbility extends EffectMorphAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.CAN_BREATHE_UNDER_WATER;
    }

    private final PotionEffect conduitEffect = new PotionEffect(PotionEffectType.CONDUIT_POWER, 20, 0, true, false);

    @Override
    protected PotionEffect getEffect()
    {
        return conduitEffect;
    }

    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        this.updateOxygen(player);
        return super.handle(player, state);
    }

    private int clamp(int min, int max, int val)
    {
        return val > max ? max : (val < min ? min : val);
    }

    private void updateOxygen(Player player)
    {
        var nmsPlayer = NmsRecord.ofPlayer(player);
        var air = nmsPlayer.getAirSupply();

        //LivingEntity#increaseAirSupply()
        air -= nmsPlayer.isInWater() ? (-5) : 5;
        air = clamp(-20, nmsPlayer.getMaxAirSupply(), air);

        nmsPlayer.setAirSupply(air);

        if (air <= -20)
        {
            nmsPlayer.hurt(nmsPlayer.getLevel().damageSources().drown(), 2);
            air = 0;
        }

        nmsPlayer.setAirSupply(air);
    }
}
