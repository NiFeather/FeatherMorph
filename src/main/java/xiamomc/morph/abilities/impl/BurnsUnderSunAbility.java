package xiamomc.morph.abilities.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.misc.DisguiseState;

public class BurnsUnderSunAbility extends NoOpOptionAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.BURNS_UNDER_SUN;
    }

    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        if (player.getWorld().getEnvironment().equals(World.Environment.NORMAL)
                && player.getEquipment().getHelmet() == null
                && player.getWorld().isDayTime()
                && player.getWorld().isClearWeather()
                && !player.isInWaterOrRainOrBubbleColumn()
                && player.getLocation().getBlock().getLightFromSky() == 15)
        {
            player.setFireTicks(200);
        }

        return true;
    }
}
