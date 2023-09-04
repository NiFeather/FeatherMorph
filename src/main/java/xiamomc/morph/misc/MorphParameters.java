package xiamomc.morph.misc;

/**
 * 用于传递给morph方法的杂项参数
 */
public class MorphParameters
{
    public boolean bypassPermission = false;
    public boolean bypassAvailableCheck = false;
    public boolean forceExecute = false;

    public MorphParameters setBypassPermission(boolean val)
    {
        this.bypassPermission = val;

        return this;
    }

    public MorphParameters setBypassAvailableCheck(boolean val)
    {
        this.bypassAvailableCheck = val;

        return this;
    }

    public MorphParameters setForceExecute(boolean val)
    {
        this.forceExecute = val;

        return this;
    }

    public static MorphParameters create()
    {
        return new MorphParameters();
    }
}
