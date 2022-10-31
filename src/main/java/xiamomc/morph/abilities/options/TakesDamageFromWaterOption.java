package xiamomc.morph.abilities.options;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.storage.skill.ISkillOption;

import java.util.Map;

public class TakesDamageFromWaterOption implements ISkillOption
{
    public TakesDamageFromWaterOption()
    {
    }

    private double damageAmount = 0d;

    public double getDamageAmount()
    {
        return damageAmount;
    }

    @Override
    public Map<String, Object> toMap()
    {
        var map = new Object2ObjectOpenHashMap<String, Object>();

        map.put("damage", damageAmount);

        return map;
    }

    @Override
    public @Nullable ISkillOption fromMap(@Nullable Map<String, Object> map)
    {
        if (map == null) return null;

        var instance = new TakesDamageFromWaterOption();

        instance.damageAmount = tryGet(map, "damage", 0d);

        return instance;
    }
}
