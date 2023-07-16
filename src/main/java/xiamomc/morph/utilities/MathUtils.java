package xiamomc.morph.utilities;

import org.bukkit.util.Vector;

public class MathUtils
{
    public static int clamp(int min, int max, int val)
    {
        return val > max ? max : (val < min ? min : val);
    }

    public static float clamp(float min, float max, float val)
    {
        return val > max ? max : (val < min ? min : val);
    }

    public static double clamp(double min, double max, double val)
    {
        return val > max ? max : (val < min ? min : val);
    }

    public static boolean vectorNotZero(Vector vector)
    {
        return vector.getX() != 0 || vector.getY() != 0 || vector.getZ() != 0;
    }

    public static double getVectorMaxVal(Vector vector)
    {
        var x = vector.getX();
        var y = vector.getY();
        var z = vector.getZ();

        return x > y ? x : y > z ? y : z;
    }

    public static int max(int a, int b, int c)
    {
        return a > b ? a : b > c ? b : c;
    }
}
