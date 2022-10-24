package xiamomc.morph.skills;

import net.kyori.adventure.key.Key;

public class SkillType
{
    private static final String nameSpace = "morph";

    public static Key INVENTORY = Key.key(nameSpace, "fake_inventory");
    public static Key EXPLODE = Key.key(nameSpace, "explode");
    public static Key LAUNCH_PROJECTIVE = Key.key(nameSpace, "launch_projective");
    public static Key APPLY_EFFECT = Key.key(nameSpace, "apply_effect");
    public static Key TELEPORT = Key.key(nameSpace, "teleport");
    public static Key EVOKER = Key.key(nameSpace, "evoker");

    public static Key UNKNOWN = Key.key(nameSpace, "unknown");
}
