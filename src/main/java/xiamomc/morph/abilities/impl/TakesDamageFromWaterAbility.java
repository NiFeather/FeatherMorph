package xiamomc.morph.abilities.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.misc.DisguiseState;

public class TakesDamageFromWaterAbility extends NoOpOptionAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.TAKES_DAMAGE_FROM_WATER;
    }

    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        if (player.isInWaterOrRainOrBubbleColumn())
            player.damage(1);

        return true;
    }
}
