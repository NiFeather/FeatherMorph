package xyz.nifeather.morph.abilities.options;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import xyz.nifeather.morph.storage.skill.ISkillOption;

public class DryoutAbilityOption implements ISkillOption
{
    public DryoutAbilityOption()
    {
    }

    public DryoutAbilityOption(boolean includeRain)
    {
        this.includeRain = includeRain;
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

    /**
     * 是否在雨中也会脱水？
     */
    @Expose
    @SerializedName("include_rain")
    public boolean includeRain = true;
}
