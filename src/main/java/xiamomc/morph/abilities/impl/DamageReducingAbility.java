package xiamomc.morph.abilities.impl;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import xiamomc.morph.MorphManager;
import xiamomc.morph.abilities.MorphAbility;
import xiamomc.morph.abilities.options.ReduceDamageOption;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.Objects;

public abstract class DamageReducingAbility<T extends ReduceDamageOption> extends MorphAbility<T>
{
    protected abstract EntityDamageEvent.DamageCause getTargetCause();

    @Resolved
    private MorphManager morphs;

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTookDamage(EntityDamageEvent e)
    {
        if (e.getEntity() instanceof Player player && appliedPlayers.contains(player))
        {
            if (e.getCause() == getTargetCause())
            {
                var state = morphs.getDisguiseStateFor(player);
                assert state != null;

                var dmgOption = getOr(
                        options.get(state.getDisguiseIdentifier()),
                        Objects::nonNull,
                        options.get(state.getSkillIdentifier())
                );

                if(dmgOption != null)
                {
                    var damage = e.getDamage();
                    var percentage = dmgOption.isPercentage();

                    damage = percentage
                            ? damage * (1 - dmgOption.getReduceAmount())
                            : damage - dmgOption.getReduceAmount();

                    e.setDamage(Math.max(0d, damage));
                }
            }
        }
    }
}
