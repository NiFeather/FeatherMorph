package xiamomc.morph.storage.skill;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import xiamomc.morph.skills.SkillType;

import java.util.Map;

public class TeleportConfiguration implements ISkillOption
{
    public TeleportConfiguration()
    {
    }

    public TeleportConfiguration(int maxDistance)
    {
        this.maxDistance = maxDistance;
    }

    @Expose
    @SerializedName("max_distance")
    private int maxDistance;

    public int getMaxDistance()
    {
        return maxDistance;
    }

    @Override
    public Map<String, Object> toMap()
    {
        var map = new Object2ObjectOpenHashMap<String, Object>();

        map.put("max_distance", (int)maxDistance);

        return map;
    }

    @Override
    public TeleportConfiguration fromMap(Map<String, Object> map)
    {
        if (map == null) return null;

        var instance = new TeleportConfiguration();

        instance.maxDistance = tryGetInt(map, "max_distance", 32);

        return instance;
    }
}
