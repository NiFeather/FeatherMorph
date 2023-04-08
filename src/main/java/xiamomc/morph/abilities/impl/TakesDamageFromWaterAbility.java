package xiamomc.morph.abilities.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.MorphAbility;
import xiamomc.morph.abilities.options.TakesDamageFromWaterOption;
import xiamomc.morph.misc.DisguiseState;

public class TakesDamageFromWaterAbility extends MorphAbility<TakesDamageFromWaterOption>
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.TAKES_DAMAGE_FROM_WATER;
    }

    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        var dmgOption = this.getOptionFor(state);

        if (dmgOption == null) return false;

        player.getScheduler().run(morphPlugin(), r ->
        {
            if (player.isInWaterOrRainOrBubbleColumn())
            {
                player.damage(dmgOption.damageAmount);
            }
        }, null);

        return true;
    }

    @Override
    protected TakesDamageFromWaterOption createOption()
    {
        return new TakesDamageFromWaterOption();
    }
}
