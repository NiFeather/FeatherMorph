package xyz.nifeather.morph.skills.options;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import xyz.nifeather.morph.storage.skill.ISkillOption;

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
    public boolean isValid()
    {
        return true;
    }
}
