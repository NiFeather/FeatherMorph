package xiamomc.morph.skills;

import org.bukkit.NamespacedKey;

public class SkillType
{
    private static final String nameSpace = "morph";

    public static final NamespacedKey INVENTORY = new NamespacedKey(nameSpace, "fake_equip");
    public static final NamespacedKey EXPLODE = new NamespacedKey(nameSpace, "explode");
    public static final NamespacedKey LAUNCH_PROJECTIVE = new NamespacedKey(nameSpace, "launch_projective");
    public static final NamespacedKey APPLY_EFFECT = new NamespacedKey(nameSpace, "apply_effect");
    public static final NamespacedKey TELEPORT = new NamespacedKey(nameSpace, "teleport");
    public static final NamespacedKey EVOKER = new NamespacedKey(nameSpace, "evoker");
    public static final NamespacedKey SONIC_BOOM = new NamespacedKey(nameSpace, "sonic_boom");

    @Deprecated(forRemoval = true)
    public static final NamespacedKey GHAST = new NamespacedKey(nameSpace, "launch_projective_ghast");

    public static final NamespacedKey WITCH = new NamespacedKey(nameSpace, "witch");

    public static final NamespacedKey UNKNOWN = new NamespacedKey(nameSpace, "unknown");
    public static final NamespacedKey NONE = new NamespacedKey(nameSpace, "none");
}
