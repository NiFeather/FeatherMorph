package xiamomc.morph.skills.options;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import xiamomc.morph.storage.skill.ISkillOption;

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

    public static NoOpConfiguration instance = new NoOpConfiguration();

    /**
     * 检查此Option是否合法
     *
     * @return 此Option是否合法
     */
    @Override
    public boolean isValid()
    {
        return true;
    }
}
