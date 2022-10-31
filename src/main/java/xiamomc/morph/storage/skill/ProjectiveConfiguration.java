package xiamomc.morph.storage.skill;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.EntityType;
import xiamomc.morph.skills.SkillType;

import java.util.Map;

public class ProjectiveConfiguration implements ISkillOption
{
    public ProjectiveConfiguration()
    {
    }

    public ProjectiveConfiguration(String name, float multiplier, String soundName, int soundDistance, int distanceLimit)
    {
        this.name = name;
        this.multiplier = multiplier;
        this.soundName = soundName;
        this.soundDistance = soundDistance;
        this.distanceLimit = distanceLimit;
    }

    public ProjectiveConfiguration(String name, float multiplier, String soundName, int soundDistance)
    {
        this(name, multiplier, soundName, soundDistance, 0);
    }

    public ProjectiveConfiguration(EntityType entityType, float multiplier, String soundName, int soundDistance)
    {
        this(entityType.getKey().asString(), multiplier, soundName, soundDistance, 0);
    }

    public ProjectiveConfiguration(EntityType entityType, float multiplier, String soundName, int soundDistance, int distanceLimit)
    {
        this(entityType.getKey().asString(), multiplier, soundName, soundDistance, distanceLimit);
    }

    @Override
    public Map<String, Object> toMap()
    {
        var map = new Object2ObjectOpenHashMap<String, Object>();

        map.put("name", name);
        map.put("speed_multiplier", multiplier);
        map.put("sound_name", soundName);
        map.put("sound_distance", soundDistance);
        map.put("max_target_distance", distanceLimit);

        return map;
    }

    @Override
    public ProjectiveConfiguration fromMap(Map<String, Object> map)
    {
        if (map == null) return null;

        var instance = new ProjectiveConfiguration();

        instance.name = "" + map.getOrDefault("name", "");
        instance.soundName = "" + map.getOrDefault("sound_name", "");

        instance.multiplier = tryGetFloat(map, "speed_multiplier", 1f);
        instance.soundDistance = tryGetInt(map, "sound_distance", 0);
        instance.distanceLimit = tryGetInt(map, "max_target_distance", 0);

        return instance;
    }

    @Expose
    private String name;

    public String getName()
    {
        return name;
    }

    @Expose
    @SerializedName("speed_multiplier")
    private float multiplier;

    public float getVectorMultiplier()
    {
        return multiplier;
    }

    @Expose
    @SerializedName("sound_name")
    private String soundName;

    public String getSoundName()
    {
        return soundName == null ? "" : soundName;
    }

    @Expose
    @SerializedName("sound_distance")
    private int soundDistance;

    public int getSoundDistance()
    {
        return soundDistance;
    }

    @Expose
    @SerializedName("max_target_distance")
    private int distanceLimit;

    public int getDistanceLimit()
    {
        return distanceLimit;
    }
}
