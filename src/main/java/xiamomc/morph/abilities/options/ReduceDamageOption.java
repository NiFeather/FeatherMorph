package xiamomc.morph.abilities.options;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import xiamomc.morph.storage.skill.ISkillOption;

public class ReduceDamageOption implements ISkillOption
{
    @Expose
    @SerializedName("amount")
    private double reduceAmount = 0d;

    @Expose
    @SerializedName("is_percentage")
    private boolean isPercentage = false;

    public ReduceDamageOption()
    {
    }

    public ReduceDamageOption(double amount)
    {
        this(amount, false);
    }

    public ReduceDamageOption(double amount, boolean isPercentage)
    {
        this.reduceAmount = amount;
        this.isPercentage = isPercentage;
    }

    public double getReduceAmount()
    {
        return reduceAmount;
    }

    public boolean isPercentage()
    {
        return isPercentage;
    }

    protected ReduceDamageOption createInstance()
    {
        return new ReduceDamageOption();
    }

    @Override
    public boolean isValid()
    {
        return !Double.isNaN(reduceAmount);
    }
}
