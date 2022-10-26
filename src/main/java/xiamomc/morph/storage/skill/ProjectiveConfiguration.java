package xiamomc.morph.storage.skill;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.entity.EntityType;

public class ProjectiveConfiguration
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
