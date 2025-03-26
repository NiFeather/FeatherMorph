package xyz.nifeather.morph.abilities.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.api.morphs.abilities.AbilityNames;
import xyz.nifeather.morph.misc.DisguiseState;

public class BurnsUnderSunAbility extends NoOpOptionAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityNames.BURNS_UNDER_SUN;
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
