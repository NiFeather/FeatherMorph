package xyz.nifeather.morph.abilities.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.abilities.MorphAbility;
import xyz.nifeather.morph.abilities.options.ExtraAirOption;
import xyz.nifeather.morph.api.morphs.abilities.AbilityNames;
import xyz.nifeather.morph.misc.DisguiseState;

public class ExtraAirAbility extends MorphAbility<ExtraAirOption>
{
    /**
     * 获取此被动技能的ID
     *
     * @return {@link NamespacedKey}
     */
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityNames.EXTRA_AIR;
    }

    @Override
    public boolean applyToPlayer(Player player, DisguiseState state)
    {
        var option = this.getOptionFor(state);
        if (option == null)
        {
            logger.error("No option set for %s under %s".formatted(this.getIdentifier(), state.getDisguiseIdentifier()));
            return false;
        }

        float airPercentage = (float)player.getRemainingAir() / (float)player.getMaximumAir();
        player.setMaximumAir(option.maxAir());

        var value = Math.round((float)player.getMaximumAir() * airPercentage);
        value = Math.clamp(value, 0, player.getMaximumAir());
        player.setRemainingAir(value);

        return super.applyToPlayer(player, state);
    }

    @Override
    public boolean revokeFromPlayer(Player player, DisguiseState state)
    {
        float airPercentage = (float)player.getRemainingAir() / (float)player.getMaximumAir();

        player.setMaximumAir(300); // See NMS Entity#getDefaultMaxAirSupply

        var value = Math.round((float)player.getMaximumAir() * airPercentage);
        value = Math.clamp(value, 0, player.getMaximumAir());
        player.setRemainingAir(value);

        return super.revokeFromPlayer(player, state);
    }

    /**
     * 更新某个玩家的被动技能
     *
     * @param player 目标玩家
     * @param state  {@link DisguiseState}
     * @return 操作是否成功
     */
    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        return true;
    }

    @Override
    public @NotNull ISkillAbilityOptionHandler<ExtraAirOption> optionHandler()
    {
        return ExtraAirOption.OPTION_HANDLER;
    }
}
