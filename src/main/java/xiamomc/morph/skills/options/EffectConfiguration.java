package xiamomc.morph.skills.options;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.storage.skill.ISkillOption;

public class EffectConfiguration implements ISkillOption
{
    public EffectConfiguration()
    {
    }

    public EffectConfiguration(String name, int multiplier, int duration,
                               boolean requiresWater, boolean showGuardian,
                               @Nullable String soundName, int soundDistance, int applyDistance)
    {
        this.name = name;
        this.multiplier = multiplier;
        this.duration = duration;
        this.acquiresWater = requiresWater;
        this.showGuardian = showGuardian;
        this.soundName = soundName;
        this.soundDistance = soundDistance;
        this.applyDistance = applyDistance;
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
    @SerializedName("acquires_water")
    private boolean acquiresWater;

    public boolean acquiresWater()
    {
        return acquiresWater;
    }

    @Expose
    @SerializedName("show_guardian")
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

    @Expose
    @SerializedName("apply_distance")
    private int applyDistance;

    public int getApplyDistance()
    {
        return applyDistance;
    }

    public int getSoundDistance()
    {
        return soundDistance;
    }

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
