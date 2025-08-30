package xyz.nifeather.morph.abilities.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.abilities.MorphAbility;
import xyz.nifeather.morph.abilities.options.DryoutAbilityOption;
import xyz.nifeather.morph.api.morphs.abilities.AbilityNames;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.utilities.MathUtils;

public class DryOutInAirAbility extends MorphAbility<DryoutAbilityOption>
{
    /**
     * 获取此被动技能的ID
     *
     * @return {@link NamespacedKey}
     */
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityNames.DRYOUT_IN_AIR;
    }

    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        this.updateOxygen(player, this.getOptionFor(state));
        return true;
    }

    @Override
    public @NotNull ISkillAbilityOptionHandler<DryoutAbilityOption> optionHandler()
    {
        return DryoutAbilityOption.OPTION_HANDLER;
    }

    private void updateOxygen(Player player, @Nullable DryoutAbilityOption option)
    {
        if (option == null)
            return;

        var air = player.getRemainingAir();

        //LivingEntity#increaseAirSupply()
        air -= (option.includeRain ? player.isInWater() : (player.isInWater() || player.isInRain())) ? (-5) : 5;
        air = MathUtils.clamp(-20, player.getMaximumAir(), air);

        if (air <= -20)
        {
            player.damage(2, DamageSource.builder(DamageType.DRY_OUT).build());
            air = 0;
        }

        player.setRemainingAir(air);
    }
}
