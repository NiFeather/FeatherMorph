package xiamomc.morph.abilities.impl;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.misc.DisguiseState;

public class SnowyAbility extends NoOpOptionAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.SNOWY;
    }

    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        var playerLocation = player.getLocation();

        var block = playerLocation.getBlock();

        if (block.getType().isAir()
                && block.canPlace(Material.SNOW.createBlockData())
                && block.getTemperature() <= 0.95)
        {
            block.setType(Material.SNOW);
        }

        player.setFreezeTicks(0);

        if (playerLocation.getBlock().getTemperature() > 1.0)
            player.setFireTicks(40);

        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTookDamage(EntityDamageEvent e)
    {
        if (e.getEntity() instanceof Player player && isPlayerApplied(player))
        {
            if (e.getCause() == EntityDamageEvent.DamageCause.FREEZE)
                e.setDamage(0d);
        }
    }
}
