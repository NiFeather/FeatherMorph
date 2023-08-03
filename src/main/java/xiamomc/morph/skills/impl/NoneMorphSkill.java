package xiamomc.morph.skills.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.skills.MorphSkill;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.skills.options.NoOpConfiguration;
import xiamomc.morph.storage.skill.SkillAbilityConfiguration;

public final class NoneMorphSkill extends MorphSkill<NoOpConfiguration>
{
    public static final NoneMorphSkill instance = new NoneMorphSkill();

    @Override
    public int executeSkill(Player player, DisguiseState state, SkillAbilityConfiguration configuration, NoOpConfiguration option)
    {
        logger.warn(configuration.getIdentifier() + "没有技能，但却被调用了executeSkill");
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
