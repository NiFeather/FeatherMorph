package xyz.nifeather.netherite.network.commands.S2C;

public class NetheriteS2CCommandNames
{
    public static final String Current = "current";
    public static final String ReAuth = "reauth";
    public static final String UnAuth = "unauth";
    public static final String SwapHands = "swap";
    public static final String Query = "query";

    public static final String Request = "request";
    public static final String RequestNew = "new";
    public static final String RequestExpire = "expire";
    public static final String RequestExpireOwner = "expire_owner";
    public static final String RequestAccept = "player_accept";
    public static final String RequestDenied = "player_deny";
    public static final String RequestSend = "send_success";

    public static final String BaseSet = "set";
    public static final String SetAggressive = "aggressive";
    public static final String SetFakeEquip = "equip";
    public static final String SetDisplayingFakeEquip = "fake_equip";
    public static final String SetProfile = "profile";
    public static final String SetSelfViewIdentifier = "selfview";
    public static final String SetSkillCooldown = "cd";
    public static final String SetSNbt = "nbt";
    public static final String SetSneaking = "sneaking";
    public static final String SetSelfViewing = "toggleself";
    public static final String SetModifyBoundingBox = "boundingbox";
    public static final String SetReach = "reach";
    public static final String SetRevealing = "reveal";
    public static final String SetAvailableAnimations = "avail_anim";
    public static final String SetAnimationDisplayName = "anim_display";

    public static final String Map = "map";
    public static final String MapPartial = "mapp";
    public static final String MapClear = "mapc";
    public static final String MapRemove = "mapr";

    public static final String Animation = "animation";

    public static final String CRMap = "crm";
    public static final String CRAdd = "cra";
    public static final String CRRemove = "crrm";
    public static final String CRClear = "crc";
    public static final String CRMeta = "crmeta";

    @Deprecated
    public static final String SetNbt = SetSNbt;


    /**
     * 已废弃
     */
    @Deprecated
    public static final String SetSpider = "spider";
}
