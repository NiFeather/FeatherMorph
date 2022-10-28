package xiamomc.morph.abilities.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.MorphAbility;
import xiamomc.morph.abilities.options.TakesDamageFromWaterOption;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.storage.skill.ISkillOption;

import java.util.Objects;

public class TakesDamageFromWaterAbility extends MorphAbility<TakesDamageFromWaterOption>
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.TAKES_DAMAGE_FROM_WATER;
    }

    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        if (player.isInWaterOrRainOrBubbleColumn())
        {
            var dmgOption = getOr(
                    options.get(state.getDisguiseIdentifier()),
                    Objects::nonNull,
                    options.get(state.getSkillIdentifier()));

            player.damage(dmgOption == null ? 1d : dmgOption.getDamageAmount());
        }

        return true;
    }

    @Override
    protected ISkillOption createOption()
    {
        return new TakesDamageFromWaterOption();
    }
}
