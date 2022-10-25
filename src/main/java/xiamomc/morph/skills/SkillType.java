package xiamomc.morph.skills;

import org.bukkit.NamespacedKey;

public class SkillType
{
    private static final String nameSpace = "morph";

    public static NamespacedKey INVENTORY = new NamespacedKey(nameSpace, "fake_inventory");
    public static NamespacedKey EXPLODE = new NamespacedKey(nameSpace, "explode");
    public static NamespacedKey LAUNCH_PROJECTIVE = new NamespacedKey(nameSpace, "launch_projective");
    public static NamespacedKey APPLY_EFFECT = new NamespacedKey(nameSpace, "apply_effect");
    public static NamespacedKey TELEPORT = new NamespacedKey(nameSpace, "teleport");
    public static NamespacedKey EVOKER = new NamespacedKey(nameSpace, "evoker");

    public static NamespacedKey UNKNOWN = new NamespacedKey(nameSpace, "unknown");
    public static NamespacedKey NONE = new NamespacedKey(nameSpace, "none");
}
