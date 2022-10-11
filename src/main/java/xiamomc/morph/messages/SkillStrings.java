package xiamomc.morph.messages;

public class SkillStrings
{
    public static FormattableMessage skillNotAvaliableString = new FormattableMessage(getKey("not_avaliable"),
            "<color:red>此伪装没有技能");

    public static FormattableMessage skillPreparing = new FormattableMessage(getKey("preparing"),
            "<color:red>技能正在准备中（ <time>秒 ）");

    //水产技能
    public static FormattableMessage notInWaterString = new FormattableMessage(getKey("not_in_water"),
            "你需要在水里才能使用此技能");

    //玩家、盔甲架技能
    public static FormattableMessage displayingPlayerInventoryString = new FormattableMessage(getKey("showing_player_inventory"),
            "正显示自己的盔甲和手持物");

    public static FormattableMessage displayingDisguiseInventoryString = new FormattableMessage(getKey("showing_disguise_inventory"),
            "正显示伪装自带的盔甲和手持物");

    //远古守卫者
    public static FormattableMessage elderGuardianCoolingDownString = new FormattableMessage(getKey("elder_guradian_cooling_down"),
            "远古守卫者的技能仍在准备中");

    //小黑
    public static FormattableMessage targetNotSuitableString = new FormattableMessage(getKey("target_not_suitable"),
            "目标太远或不合适");

    //潜影贝
    public static FormattableMessage difficultyIsPeacefulString = new FormattableMessage(getKey("difficulty_is_peaceful"),
            "世界难度为和平");

    public static FormattableMessage noTargetString = new FormattableMessage(getKey("no_target"),
            "视线<distance>格以内没有实体");

    private static String getKey(String key)
    {
        return "morph.skill." + key;
    }
}
