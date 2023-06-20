package xiamomc.morph.events;

import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.RevealingHandler;
import xiamomc.morph.events.api.gameplay.PlayerMorphEvent;
import xiamomc.morph.events.api.gameplay.PlayerUnMorphEvent;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.pluginbase.Annotations.Resolved;

public class RevealingEventProcessor extends MorphPluginObject implements Listener
{
    @Resolved(shouldSolveImmediately = true)
    private RevealingHandler handler;

    @Resolved(shouldSolveImmediately = true)
    private PlayerTracker tracker;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        handler.updateStatePlayerInstance(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent e)
    {
        if (!e.hasBlock()) return;
        if (!e.getClickedBlock().getType().isInteractable()) return;

        var player = e.getPlayer();
        if (tracker.isDuplicatedRightClick(player)) return;

        var revealingState = handler.getRevealingState(player);
        revealingState.addBaseValue(RevealingHandler.RevealingDiffs.INTERACT);
    }

    @EventHandler
    public void onPlayerDeath(EntityDeathEvent e)
    {
        if (!(e.getEntity() instanceof Player player)) return;

        var revState = handler.getRevealingState(player);
        revState.setBaseValue(0);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent e)
    {
        var revState = handler.getRevealingState(e.getPlayer());
        revState.addBaseValue(RevealingHandler.RevealingDiffs.INTERACT_ENTITY);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e)
    {
        if (!(e.getEntity() instanceof Player player)) return;

        var revState = handler.getRevealingState(player);
        revState.addBaseValue(RevealingHandler.RevealingDiffs.ON_DAMAGE);
    }

    @EventHandler
    public void onPlayerDamageEntities(EntityDamageByEntityEvent e)
    {
        if (!(e.getDamager() instanceof Player player)) return;

        var revState = handler.getRevealingState(player);

        revState.addBaseValue(RevealingHandler.RevealingDiffs.ON_DAMAGE);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e)
    {
        // 玩家破坏方块 -> 揭示值+5
        var revealingState = handler.getRevealingState(e.getPlayer());
        revealingState.addBaseValue(RevealingHandler.RevealingDiffs.BLOCK_BREAK);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e)
    {
        var revealingState = handler.getRevealingState(e.getPlayer());
        revealingState.addBaseValue(RevealingHandler.RevealingDiffs.BLOCK_PLACE);
    }

    @EventHandler
    public void onPlayerMorph(PlayerMorphEvent e)
    {
        var player = e.getPlayer();
        var mobsNearby = player.getNearbyEntities(16, 16, 16);
        var nmsPlayer = NmsRecord.ofPlayer(player);

        var revealingState = this.handler.getRevealingState(player);
        revealingState.addBaseValue(RevealingHandler.RevealingLevel.SUSPECT.getValue() * 0.1f, true);

        if (!mobsNearby.isEmpty())
        {
            // 如果变形时附近有生物在向着玩家，并且距离过近，那么直接拉满揭示值
            for (var entity : mobsNearby)
            {
                if (!(entity instanceof CraftMob craftMob)) continue;
                if (craftMob.getHandle().getTarget() == nmsPlayer)
                {
                    var base = revealingState.getBaseValue();
                    revealingState.setBaseValue(Math.max(base, RevealingHandler.RevealingDiffs.ALREADY_TARGETED));
                    break;
                }
            }
        }

        revealingState.bindingState = e.getState();
    }

    @EventHandler
    public void onPlayerUnmorph(PlayerUnMorphEvent e)
    {
        var revealingState = this.handler.getRevealingState(e.getPlayer());
        revealingState.bindingState = null;
    }
}
