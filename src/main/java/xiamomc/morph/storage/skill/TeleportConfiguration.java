package xiamomc.morph.storage.skill;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TeleportConfiguration
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
}
