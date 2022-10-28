package xiamomc.morph.abilities.options;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.storage.skill.ISkillOption;

import java.util.Map;

public class FlyOption implements ISkillOption
{
    public FlyOption()
    {
    }

    public FlyOption(float speed)
    {
        this.flyingSpeed = speed;
    }

    private float flyingSpeed;

    public float getFlyingSpeed()
    {
        return flyingSpeed;
    }

    @Override
    public Map<String, Object> toMap()
    {
        var map = new Object2ObjectOpenHashMap<String, Object>();

        map.put("fly_speed", flyingSpeed);

        return map;
    }

    @Override
    public @Nullable ISkillOption fromMap(@Nullable Map<String, Object> map)
    {
        if (map == null) return null;

        var instance = new FlyOption();

        instance.flyingSpeed = (float) (double) map.get("fly_speed");

        return instance;
    }
}
