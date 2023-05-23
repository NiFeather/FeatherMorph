package xiamomc.morph.abilities.options;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import xiamomc.morph.storage.skill.ISkillOption;

public class FlyOption implements ISkillOption
{
    public FlyOption()
    {
    }

    public FlyOption(float speed)
    {
        this.flyingSpeed = speed;
    }

    @Expose
    @SerializedName("fly_speed")
    private float flyingSpeed = Float.NaN;

    public float getFlyingSpeed()
    {
        return flyingSpeed;
    }

    @Expose
    @SerializedName("hunger_consume_multiplier")
    private float hungerConsumeMultiplier = 1f;

    public float getHungerConsumeMultiplier()
    {
        return hungerConsumeMultiplier;
    }

    public void setHungerConsumeMultiplier(float newVal)
    {
        hungerConsumeMultiplier = newVal;
    }

    @Expose
    @SerializedName("minimum_hunger")
    private int minimumHunger = 6;

    public int getMinimumHunger()
    {
        return minimumHunger;
    }

    public void setMinimumHunger(int newVal)
    {
        minimumHunger = newVal;
    }

    @Override
    public boolean isValid()
    {
        return !Float.isNaN(hungerConsumeMultiplier) && !Float.isNaN(flyingSpeed);
    }
}
