package xiamomc.morph.utilities;

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
}
