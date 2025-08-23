package xyz.nifeather.morph.abilities.impl;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import xyz.nifeather.morph.abilities.MorphAbility;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;

public abstract class OnAttackAbility<T extends ISkillAbilityOption> extends MorphAbility<T>
{
    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamagedByEntity(EntityDamageByEntityEvent e)
    {
        if (!(e.getDamager() instanceof Player player)) return;
        if (!(e.getEntity() instanceof LivingEntity livingEntity)) return;
        if (!isPlayerApplied(player)) return;

        this.onAttack(livingEntity, player);
    }

    protected abstract void onAttack(LivingEntity hurt, Player source);
}
