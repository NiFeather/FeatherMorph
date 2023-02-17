package xiamomc.morph.skills.options;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import xiamomc.morph.storage.skill.ISkillOption;

public class ExplosionConfiguration implements ISkillOption
{
    public ExplosionConfiguration()
    {
    }

    public ExplosionConfiguration(boolean killsSelf, int strength, boolean setsFire, int delay)
    {
        this.killsSelf = killsSelf;
        this.strength = strength;
        this.setsFire = setsFire;
        this.executeDelay = delay;
    }

    @Expose
    @SerializedName("kills_self")
    private boolean killsSelf = true;

    @Expose
    @SerializedName("delay")
    public int executeDelay = 30;

    public boolean killsSelf()
    {
        return killsSelf;
    }

    @Expose
    private int strength = 3;

    public int getStrength()
    {
        return strength;
    }

    @Expose
    @SerializedName("sets_fire")
    private boolean setsFire;

    public boolean setsFire()
    {
        return setsFire;
    }

    @Override
    public boolean isValid()
    {
        return true;
    }
}
