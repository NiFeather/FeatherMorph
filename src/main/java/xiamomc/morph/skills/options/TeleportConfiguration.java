package xiamomc.morph.skills.options;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import xiamomc.morph.storage.skill.ISkillOption;

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
    public boolean isValid()
    {
        return true;
    }
}
