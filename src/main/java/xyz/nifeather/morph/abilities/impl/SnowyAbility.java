package xyz.nifeather.morph.abilities.impl;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.api.morphs.abilities.AbilityNames;
import xyz.nifeather.morph.misc.DisguiseState;

import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class SnowyAbility extends NoOpOptionAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityNames.SNOWY;
    }

    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        var playerLocation = player.getLocation();

        var block = playerLocation.getBlock();

        if (block.getType().isAir()
                && !playerBlocked(player)
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

    private static final Map<Player, Stack<Object>> blockedPlayersMap = new ConcurrentHashMap<>();

    public static boolean playerBlocked(Player player)
    {
        var stack = blockedPlayersMap.getOrDefault(player, null);
        return stack != null && !stack.isEmpty();
    }

    public static void blockPlayer(Player player, Object requestSource)
    {
        var stack = blockedPlayersMap.getOrDefault(player, null);
        if (stack == null)
        {
            stack = new Stack<>();
            blockedPlayersMap.put(player, stack);
        }

        if (!stack.contains(requestSource))
            stack.push(requestSource);
    }

    public static void unBlockPlayer(Player player, Object requestSource)
    {
        var stack = blockedPlayersMap.getOrDefault(player, new Stack<>());
        stack.remove(requestSource);
    }
}
