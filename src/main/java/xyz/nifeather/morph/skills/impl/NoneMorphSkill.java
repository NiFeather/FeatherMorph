package xyz.nifeather.morph.skills.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.skills.MorphSkill;
import xyz.nifeather.morph.skills.SkillType;
import xyz.nifeather.morph.skills.options.NoOpConfiguration;
import xyz.nifeather.morph.storage.skill.SkillAbilityConfiguration;

public final class NoneMorphSkill extends MorphSkill<NoOpConfiguration>
{
    public static final NoneMorphSkill instance = new NoneMorphSkill();

    @Override
    public int executeSkill(Player player, DisguiseState state, SkillAbilityConfiguration configuration, NoOpConfiguration option)
    {
        logger.warn(state.getDisguiseIdentifier() + "没有技能，但却被调用了executeSkill");
        Thread.dumpStack();

        return Integer.MAX_VALUE;
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillType.NONE;
    }

    private final NoOpConfiguration option = new NoOpConfiguration();

    @Override
    public NoOpConfiguration getOptionInstance()
    {
        return option;
    }
}
