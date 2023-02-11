package xiamomc.morph.skills.options;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import xiamomc.morph.storage.skill.ISkillOption;

import java.util.Map;

public class ExplosionConfiguration implements ISkillOption
{
    public ExplosionConfiguration()
    {
    }

    public ExplosionConfiguration(boolean killsSelf, int strength, boolean setsFire)
    {
        this.killsSelf = killsSelf;
        this.strength = strength;
        this.setsFire = setsFire;
    }

    @Expose
    @SerializedName("kills_self")
    private boolean killsSelf = true;

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
