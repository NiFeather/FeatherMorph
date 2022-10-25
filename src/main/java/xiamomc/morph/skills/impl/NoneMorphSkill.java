package xiamomc.morph.skills.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.skills.MorphSkill;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.skills.configurations.SkillConfiguration;

public class NoneMorphSkill extends MorphSkill
{
    @Override
    public int executeSkill(Player player, SkillConfiguration configuration)
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
}
