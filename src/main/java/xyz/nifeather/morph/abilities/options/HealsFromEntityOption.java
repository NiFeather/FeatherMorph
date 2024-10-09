package xyz.nifeather.morph.abilities.options;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.storage.skill.ISkillOption;

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
                && Double.isFinite(healAmount) && Double.isFinite(distance) && entityIdentifier != null;
    }

    @Expose
    @SerializedName("max_percentage")
    public double maxPercentage;

    @Expose
    @SerializedName("damage_when_destroyed")
    public float damageWhenDestroyed;

    @Expose
    @SerializedName("amount")
    public double healAmount;

    @Expose
    @SerializedName("distance")
    public double distance;

    @Expose
    @SerializedName("entity_type")
    public String entityIdentifier;

    @Nullable
    public EntityType entityType;

    public HealsFromEntityOption()
    {
    }

    public HealsFromEntityOption(double maxPercentage, float damageWhenDestroyed, double healAmount, double distance, String entityIdentifier)
    {
        this.maxPercentage = maxPercentage;
        this.damageWhenDestroyed = damageWhenDestroyed;
        this.healAmount = healAmount;
        this.distance = distance;
        this.entityIdentifier = entityIdentifier;
    }
}
