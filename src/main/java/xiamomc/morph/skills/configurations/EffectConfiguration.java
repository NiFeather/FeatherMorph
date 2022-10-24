package xiamomc.morph.skills.configurations;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EffectConfiguration
{
    public EffectConfiguration()
    {
    }

    public EffectConfiguration(String name, int multiplier, int duration, boolean requiresWater, boolean showGuardian, @Nullable String soundName, int soundDistance)
    {
        this.name = name;
        this.multiplier = multiplier;
        this.duration = duration;
        this.acquiresWater = requiresWater;
        this.showGuardian = showGuardian;
        this.soundName = soundName;
        this.soundDistance = soundDistance;
    }

    //效果名称
    @Expose
    private String name = "";

    public String getName()
    {
        return name;
    }

    @Expose
    private int multiplier;

    public int getMultiplier()
    {
        return multiplier;
    }

    @Expose
    private int duration;

    public int getDuration()
    {
        return duration;
    }

    @Expose
    private boolean acquiresWater;

    public boolean acquiresWater()
    {
        return acquiresWater;
    }

    @Expose
    @SerializedName("showGuardian")
    private boolean showGuardian;

    public boolean showGuardian()
    {
        return showGuardian;
    }

    @Expose
    @Nullable
    @SerializedName("sound")
    private String soundName;

    @NotNull
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
}
