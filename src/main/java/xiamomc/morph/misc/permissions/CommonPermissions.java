package xiamomc.morph.misc.permissions;

public class CommonPermissions
{
    private final static String PERM_ROOT = "xiamomc.morph.";

    public final static String SEND_REQUEST = PERM_ROOT + "request.send";
    public final static String ACCEPT_REQUEST = PERM_ROOT + "request.accept";
    public final static String DENY_REQUEST = PERM_ROOT + "request.deny";

    public final static String MORPH = PERM_ROOT + "morph";
    public final static String UNMORPH = PERM_ROOT + "unmorph";

    public final static String SKILL = PERM_ROOT + "skill";
    public final static String HEAD_MORPH = PERM_ROOT + "headmorph";

    public final static String CHAT_OVERRIDE = PERM_ROOT + "chatoverride";
    public final static String CHAT_OVERRIDE_REVEAL = CHAT_OVERRIDE + ".reveal";

    public final static String MIRROR = PERM_ROOT + "mirror";
    public final static String MIRROR_IMMUNE = PERM_ROOT + "mirror.immune";

    public final static String DISGUISE_REVEALING = PERM_ROOT + "disguise_revealing";
}
