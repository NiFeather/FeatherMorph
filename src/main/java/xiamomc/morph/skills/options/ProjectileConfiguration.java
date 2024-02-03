package xiamomc.morph.skills.options;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.entity.EntityType;
import xiamomc.morph.storage.skill.ISkillOption;

public class ProjectileConfiguration implements ISkillOption
{
    public ProjectileConfiguration()
    {
    }

    public ProjectileConfiguration(String name, float multiplier, String soundName, int soundDistance, int distanceLimit)
    {
        this.name = name;
        this.multiplier = multiplier;
        this.soundName = soundName;
        this.soundDistance = soundDistance;
        this.distanceLimit = distanceLimit;
    }

    public ProjectileConfiguration(String name, float multiplier, String soundName, int soundDistance)
    {
        this(name, multiplier, soundName, soundDistance, 0);
    }

    public ProjectileConfiguration(EntityType entityType, float multiplier, String soundName, int soundDistance)
    {
        this(entityType.getKey().asString(), multiplier, soundName, soundDistance, 0);
    }

    public ProjectileConfiguration(EntityType entityType, float multiplier, String soundName, int soundDistance, int distanceLimit)
    {
        this(entityType.getKey().asString(), multiplier, soundName, soundDistance, distanceLimit);
    }

    public ProjectileConfiguration withDelay(int delay)
    {
        this.executeDelay = delay;
        return this;
    }

    public ProjectileConfiguration withWarningSound(String soundName)
    {
        this.preLaunchSoundName = soundName;
        return this;
    }

    @Expose
    private String name;

    public String getName()
    {
        return name;
    }

    @Expose
    @SerializedName("speed_multiplier")
    private float multiplier = 1f;

    public float getVectorMultiplier()
    {
        return multiplier;
    }

    @Expose
    @SerializedName("warning_sound_name")
    private String preLaunchSoundName;

    public String getPreLaunchSoundName()
    {
        return preLaunchSoundName == null ? "" : preLaunchSoundName;
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

    @Expose
    @SerializedName("delay")
    public int executeDelay;

    public int getDistanceLimit()
    {
        return distanceLimit;
    }

    @Override
    public boolean isValid()
    {
        return true;
    }
}
