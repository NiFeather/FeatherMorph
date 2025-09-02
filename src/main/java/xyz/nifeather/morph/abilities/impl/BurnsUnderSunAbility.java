package xyz.nifeather.morph.abilities.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.api.morphs.abilities.AbilityNames;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.utilities.ItemUtils;

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
                && ItemUtils.itemOrAir(player.getEquipment().getHelmet()).getType().isAir()
                && player.getWorld().isDayTime()
                && player.getWorld().isClearWeather()
                && !(player.isInWater() || player.isInRain())
                && player.getLocation().getBlock().getLightFromSky() == 15)
        {
            player.setFireTicks(200);
        }

        return true;
    }
}
