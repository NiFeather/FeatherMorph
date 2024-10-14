package xyz.nifeather.morph.abilities.impl;

import io.papermc.paper.event.entity.WardenAngerChangeEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.AbilityType;

public class WardenLessAwareAbility extends NoOpOptionAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.WARDEN_LESS_AWARE;
    }

    @EventHandler
    public void onWardenAngerChange(WardenAngerChangeEvent e)
    {
        if (e.getTarget() instanceof Player player && this.isPlayerApplied(player))
        {
            //不处理愤怒值大于等于80的事件
            if (e.getNewAnger() >= 80 || e.getOldAnger() >= 80) return;

            float diff = e.getNewAnger() - e.getOldAnger();

            //将变动缩减为原来的25%
            if (diff > 0)
            {
                diff *= 0.25;

                //确保diff最小为1
                diff = Math.max(1, diff);
            }

            //logger.warn("DIFF: " + diff + " NEW " + e.getNewAnger() + " OLD " + e.getOldAnger() + " APPLIED " + (int)(e.getOldAnger() + diff));
            e.setNewAnger(e.getOldAnger() + (int) diff);
        }
    }
}
