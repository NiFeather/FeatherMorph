package xiamomc.morph.misc;

import me.libraryaddict.disguise.disguisetypes.Disguise;

public class DisguiseUtils
{
    private static String customDataTagName = "XIAMO_MORPH";

    public static void addTrace(Disguise disguise)
    {
        disguise.addCustomData(customDataTagName, true);
    }

    public static boolean isTracing(Disguise disguise)
    {
        return Boolean.TRUE.equals(disguise.getCustomData(customDataTagName));
    }
}
