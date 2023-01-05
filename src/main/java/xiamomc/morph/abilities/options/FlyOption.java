package xiamomc.morph.abilities.options;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
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

    private float hungerConsumeMultiplier;

    public float getHungerConsumeMultiplier()
    {
        return hungerConsumeMultiplier;
    }

    public void setHungerConsumeMultiplier(float newVal)
    {
        hungerConsumeMultiplier = newVal;
    }

    private int minimumHunger;

    public int getMinimumHunger()
    {
        return minimumHunger;
    }

    public void setMinimumHunger(int newVal)
    {
        minimumHunger = newVal;
    }

    @Override
    public Map<String, Object> toMap()
    {
        var map = new Object2ObjectOpenHashMap<String, Object>();

        map.put("fly_speed", flyingSpeed);
        map.put("hunger_consume_multiplier", hungerConsumeMultiplier);
        map.put("minimum_hunger", minimumHunger);

        return map;
    }

    @Override
    public @Nullable ISkillOption fromMap(@Nullable Map<String, Object> map)
    {
        if (map == null) return null;

        var instance = new FlyOption();

        instance.flyingSpeed = tryGetFloat(map, "fly_speed", Float.NaN);
        instance.hungerConsumeMultiplier = tryGetFloat(map, "hunger_consume_multiplier", 1f);
        instance.minimumHunger = tryGetInt(map, "minimum_hunger", 6);

        instance.hungerConsumeMultiplier = Math.max(0, instance.hungerConsumeMultiplier);

        return instance;
    }
}
