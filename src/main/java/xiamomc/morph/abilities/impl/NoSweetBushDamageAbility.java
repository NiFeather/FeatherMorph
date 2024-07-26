package xiamomc.morph.abilities.impl;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityType;

public class NoSweetBushDamageAbility extends NoOpOptionAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.NO_SWEET_BUSH_DAMAGE;
    }

    @EventHandler
    private void onPlayerDamage(EntityDamageByBlockEvent e)
    {
        if (e.getEntity() instanceof Player player && this.isPlayerApplied(player))
        {
            var damagerType = e.getDamager() == null ? Material.AIR : e.getDamager().getType();

            if (e.getCause() == EntityDamageEvent.DamageCause.CONTACT && damagerType == Material.SWEET_BERRY_BUSH)
                e.setCancelled(true);
        }
    }
}
