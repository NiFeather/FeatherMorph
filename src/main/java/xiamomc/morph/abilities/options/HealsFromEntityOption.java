package xiamomc.morph.abilities.options;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import xiamomc.morph.storage.skill.ISkillOption;

public class HealsFromEntityOption implements ISkillOption
{
    /**
     * 检查此Option是否合法
     *
     * @return 此Option是否合法
     */
    @Override
    public boolean isValid()
    {
        return maxPercentage <= 1d && Float.isFinite(damageWhenDestroyed)
                && Double.isFinite(healAmount) && Double.isFinite(distance);
    }

    @Expose
    @SerializedName("max_percentage")
    public double maxPercentage = 1.0d;

    @Expose
    @SerializedName("damage_when_destroyed")
    public float damageWhenDestroyed = 10f;

    @Expose
    @SerializedName("amount")
    public double healAmount = 0.05d;

    @Expose
    @SerializedName("distance")
    public double distance = 32d;

    @Expose
    @SerializedName("entity_type")
    public String entityIdentifier = "minecraft:end_crystal";
}
