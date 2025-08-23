package xyz.nifeather.morph.abilities.impl.dmgReduce;

import org.bukkit.NamespacedKey;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.abilities.impl.DamageReducingAbility;
import xyz.nifeather.morph.abilities.options.ReduceDamageOption;
import xyz.nifeather.morph.api.morphs.abilities.AbilityNames;

public class ReduceFallDamageAbility extends DamageReducingAbility<ReduceDamageOption>
{
    @Override
    public @NotNull ISkillAbilityOptionHandler<ReduceDamageOption> optionHandler()
    {
        return ReduceDamageOption.OPTION_HANDLER;
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityNames.REDUCES_FALL_DAMAGE;
    }

    @Override
    protected EntityDamageEvent.DamageCause getTargetCause()
    {
        return EntityDamageEvent.DamageCause.FALL;
    }
}
