package xyz.nifeather.morph.messages;

import xiamomc.pluginbase.Messages.FormattableMessage;

public class SkillStrings extends AbstractMorphStrings
{
    public static FormattableMessage skillNotAvaliableString()
    {
        return getFormattable(getKey("not_avaliable"),
                "<color:red>此伪装没有技能");
    }

    public static FormattableMessage exceptionOccurredString()
    {
        return getFormattable(getKey("exception_occurred"),
                "<color:red>执行技能时出现问题");
    }

    public static FormattableMessage skillPreparing()
    {
        return getFormattable(getKey("preparing"),
                "<color:red>技能正在准备中（ <time>秒 ）");
    }

    //水产技能
    public static FormattableMessage notInWaterString()
    {
        return getFormattable(getKey("not_in_water"),
                "你需要在水里才能使用此技能");
    }

    //玩家、盔甲架技能
    public static FormattableMessage displayingPlayerInventoryString()
    {
        return getFormattable(getKey("showing_player_inventory"),
                "正显示自己的盔甲和手持物");
    }

    public static FormattableMessage displayingDisguiseInventoryString()
    {
        return getFormattable(getKey("showing_disguise_inventory"),
                "正显示伪装自带的盔甲和手持物");
    }

    //小黑
    public static FormattableMessage targetNotSuitableString()
    {
        return getFormattable(getKey("target_not_suitable"),
                "目标太远或不合适");
    }

    //潜影贝
    public static FormattableMessage difficultyIsPeacefulString()
    {
        return getFormattable(getKey("difficulty_is_peaceful"),
                "世界难度为和平");
    }

    public static FormattableMessage noTargetString()
    {
        return getFormattable(getKey("no_target"),
                "视线<distance>格以内没有实体");
    }

    //苦力怕
    public static FormattableMessage explodeFailString()
    {
        return getFormattable(getKey("explode_fail"),
                "一股神秘的力量阻止了你的爆炸");
    }

    public static FormattableMessage skillNotAvailableString()
    {
        return getFormattable(getKey("not_available"),
                "目前不能使用技能");
    }

    private static String getKey(String key)
    {
        return "morph.skill." + key;
    }
}
