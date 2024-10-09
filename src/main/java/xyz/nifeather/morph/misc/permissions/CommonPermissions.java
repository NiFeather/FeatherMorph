package xyz.nifeather.morph.misc.permissions;

import xyz.nifeather.morph.misc.DisguiseTypes;

public class CommonPermissions
{
    private final static String PERM_ROOT = "feathermorph.";

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

    public final static String CHECK_UPDATE = PERM_ROOT + "check_update";

    public final static String LOOKUP = PERM_ROOT + "lookup";

    public final static String ACCESS_SKIN_CACHE = PERM_ROOT + "skin_cache";

    public final static String MAKE_DISGUISE_TOOL = PERM_ROOT + "make_disguise_tool";

    public final static String SET_BACKEND = PERM_ROOT + "switch_backend";

    public final static String SET_OPTIONS = PERM_ROOT + "toggle";

    public final static String QUERY_STATES = PERM_ROOT + "query";
    public final static String CHECK_STAT = PERM_ROOT + "stat";
    public final static String DO_RELOAD = PERM_ROOT + "reload";

    public final static String MANAGE_DISGUISES = PERM_ROOT + "manage";
    public final static String MANAGE_GRANT_DISGUISE = MANAGE_DISGUISES + ".grant";
    public final static String MANAGE_REVOKE_DISGUISE = MANAGE_DISGUISES + ".revoke";
    public final static String MANAGE_UNMORPH_DISGUISE = MANAGE_DISGUISES + ".unmorph";
    public final static String MANAGE_MORPH_DISGUISE = MANAGE_DISGUISES + ".morph";

    public final static String CAN_FLY = PERM_ROOT + "can_fly";
    public final static String ALWAYS_CAN_FLY = PERM_ROOT + "can_fly.always";

    public final static String ADMIN = PERM_ROOT + "admin";

    public static String skillPermissionOf(String skillIdentifier, String disguiseIdentifier)
    {
        if (disguiseIdentifier.startsWith(DisguiseTypes.PLAYER.getNameSpace()))
            disguiseIdentifier = "player:all";

        return PERM_ROOT
                + "skill"
                + "."
                + disguiseIdentifier.replace(":", ".")
                + "."
                + skillIdentifier.replace(":", ".");
    }

    public static String abilityPermissionOf(String abilityIdentifier, String disguiseIdentifier)
    {
        if (disguiseIdentifier.startsWith(DisguiseTypes.PLAYER.getNameSpace()))
            disguiseIdentifier = "player:all";

        return PERM_ROOT
                + "ability"
                + "."
                + disguiseIdentifier.replace(":", ".")
                + "."
                + abilityIdentifier.replace(":", ".");
    }

    public static String animationPermissionOf(String animationId, String disguiseID)
    {
        if (disguiseID.startsWith(DisguiseTypes.PLAYER.getNameSpace()))
            disguiseID = "player:all";

        return PERM_ROOT
                + "emote"
                + "."
                + disguiseID.replace(":", ".")
                + "."
                + animationId.replace(":", ".");
    }

    public static String CanFlyIn(String worldName)
    {
        return CAN_FLY + ".in." + worldName;
    }
}
