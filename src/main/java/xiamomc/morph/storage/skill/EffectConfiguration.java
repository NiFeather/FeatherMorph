package xiamomc.morph.storage.skill;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.skills.SkillType;

import java.util.Map;

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

    @Override
    public Map<String, Object> toMap()
    {
        var map = new Object2ObjectOpenHashMap<String, Object>();

        map.put("name", name);
        map.put("multiplier", multiplier);
        map.put("duration", duration);
        map.put("acquires_water", acquiresWater);
        map.put("show_guardian", showGuardian);
        map.put("sound", soundName);
        map.put("sound_distance", soundDistance);
        map.put("apply_distance", applyDistance);

        return map;
    }

    @Override
    public EffectConfiguration fromMap(Map<String, Object> map)
    {
        if (map == null) return null;

        var instance = new EffectConfiguration();

        instance.name = (String) map.getOrDefault("name", "none");
        instance.multiplier = (int) (double) map.getOrDefault("multiplier", 0);
        instance.duration = (int) (double) map.getOrDefault("duration", 0);
        instance.acquiresWater = (boolean) map.getOrDefault("acquires_water", false);
        instance.showGuardian = (boolean) map.getOrDefault("show_guardian", false);
        instance.soundName = (String) map.getOrDefault("sound_name", "none");
        instance.soundDistance = (int) (double) map.getOrDefault("sound_distance", 0);
        instance.applyDistance = (int) (double) map.getOrDefault("apply_distance", 0);

        return instance;
    }
}
