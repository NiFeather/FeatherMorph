package xiamomc.morph.abilities.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.MorphAbility;

public class NoFallDamageAbility extends MorphAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.NO_FALL_DAMAGE;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTookDamage(EntityDamageEvent e)
    {
        if (e.getEntity() instanceof Player player && appliedPlayers.contains(player))
        {
            if (e.getCause() == EntityDamageEvent.DamageCause.FALL)
                e.setDamage(0d);
        }
    }
}
