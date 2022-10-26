package xiamomc.morph.storage.skill;

import com.google.gson.annotations.Expose;

public class ExplosionConfiguration
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
    private boolean killsSelf;

    public boolean killsSelf()
    {
        return killsSelf;
    }

    @Expose
    private int strength;

    public int getStrength()
    {
        return strength;
    }

    @Expose
    private boolean setsFire;

    public boolean setsFire()
    {
        return setsFire;
    }
}
