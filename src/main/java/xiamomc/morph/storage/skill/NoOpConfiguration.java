package xiamomc.morph.storage.skill;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import xiamomc.morph.skills.IMorphSkill;
import xiamomc.morph.skills.SkillType;

import java.util.Map;

public class NoOpConfiguration implements ISkillOption
{
    @Override
    public Map<String, Object> toMap()
    {
        return new Object2ObjectOpenHashMap<>();
    }

    @Override
    public NoOpConfiguration fromMap(Map<String, Object> map)
    {
        return new NoOpConfiguration();
    }
}
