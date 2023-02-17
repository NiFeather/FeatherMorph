package xiamomc.morph.skills.impl;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.skills.SkillType;

@Deprecated
public class GhastMorphSkill extends LaunchProjectiveMorphSkill
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillType.GHAST;
    }
}
