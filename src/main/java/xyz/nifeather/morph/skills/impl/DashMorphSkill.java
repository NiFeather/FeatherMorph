package xyz.nifeather.morph.skills.impl;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.api.morphs.skills.SkillNames;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.SkillStrings;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.ExecutionErrorException;
import xyz.nifeather.morph.skills.MorphSkill;
import xyz.nifeather.morph.skills.options.DashConfiguration;

public class DashMorphSkill extends MorphSkill<DashConfiguration>
{
    /**
     * 执行变形形态的主动技能
     *
     * @param player 玩家
     * @param state  {@link DisguiseState}
     * @param option 此技能的详细设置
     * @return The override cooldown time if greater than 0
     * @throws ExecutionErrorException There's an error while executing the skill
     */
    @Override
    public int executeSkill(Player player, DisguiseState state, DashConfiguration option) throws ExecutionErrorException
    {
        boolean requiresWater = option.requireWater();
        double dashMultiplier = option.dashMultiplier(); // default 0.458
        String dashSound = option.dashSound(); // for debug: "entity.nautilus.dash"

        if (requiresWater && !player.isInWater())
        {
            MessageUtils.send(player, SkillStrings.notInWaterString());
            return 20;
        }

        var playerDirection = player.getEyeLocation().getDirection()
                .setY(0)
                .normalize()
                .multiply(dashMultiplier);

        player.setVelocity(player.getVelocity().add(playerDirection));

        Sound advSound = Sound.sound()
                .volume(0.6f).pitch(1)
                .type(Key.key(dashSound))
                .build();

        player.getWorld().playSound(advSound, Sound.Emitter.self());

        return 0;
    }

    /**
     * 获取要应用的技能ID
     *
     * @return 技能ID
     */
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillNames.DASH;
    }

    @Override
    public ISkillAbilityOptionHandler<DashConfiguration> optionHandler()
    {
        return DashConfiguration.OPTION_HANDLER;
    }
}
