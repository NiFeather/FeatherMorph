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
        return 0;
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillType.NONE;
    }
}
