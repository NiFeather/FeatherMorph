package xiamomc.morph.abilities.options;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import xiamomc.morph.storage.skill.ISkillOption;

public class TakesDamageFromWaterOption implements ISkillOption
{
    public TakesDamageFromWaterOption()
    {
    }

    @Expose
    @SerializedName("damage")
    public double damageAmount = 1d;

    @Override
    public boolean isValid()
    {
        return !Double.isNaN(damageAmount);
    }
}
