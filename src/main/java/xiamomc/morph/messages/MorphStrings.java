package xiamomc.morph.messages;

import xiamomc.pluginbase.messages.FormattableMessage;
import xiamomc.pluginbase.messages.IStrings;

import java.text.Format;

public class MorphStrings implements IStrings
{
    public static FormattableMessage morphSuccessString()
    {
        return new FormattableMessage(getKey("morph_success"),
                "成功伪装为<what>！");
    }

    public static FormattableMessage morphVisibleAfterStandup()
    {
        return new FormattableMessage(getKey("morph_visible_after_standup"),
                "伪装将在起身后对自己可见");
    }

    public static FormattableMessage disguisingAsString()
    {
        return new FormattableMessage(getKey("disguising_as"),
                "正伪装为<what>");
    }

    public static FormattableMessage morphUnlockedString()
    {
        return new FormattableMessage(getKey("morph_unlocked"),
                "<color:green>✔ 已解锁<what>的伪装！");
    }

    public static FormattableMessage morphLockedString()
    {
        return new FormattableMessage(getKey("morph_locked"),
                "<color:red>❌ 已失去<what>的伪装！");
    }

    public static FormattableMessage commandHintString()
    {
        return new FormattableMessage(getKey("command_hint"),
                "不知道如何使用伪装? 发送 /mmorph help 即可查看！");
    }

    public static FormattableMessage disguisingWithSkillPreparingString()
    {
        return new FormattableMessage(getKey("disguising_as_skill_preparing"),
                "<color:#eeb565>正伪装为<what>");
    }

    public static FormattableMessage disguisingWithSkillAvaliableString()
    {
        return new FormattableMessage(getKey("disguising_as_skill_avaliable"),
                "<color:#8fe98d>正伪装为<what>");
    }

    public static FormattableMessage unMorphSuccessString()
    {
        return new FormattableMessage(getKey("unmorph_success"),
                "已取消伪装");
    }

    public static FormattableMessage skillHintString()
    {
        return new FormattableMessage(getKey("skill_hint"),
                "小提示: 手持胡萝卜钓竿蹲下右键可以使用当前伪装的主动技能");
    }


    public static FormattableMessage invalidIdentityString()
    {
        return new FormattableMessage(getKey("invalid_id"),
                "<color:red>此ID不能用于伪装");
    }

    public static FormattableMessage parseErrorString()
    {
        return new FormattableMessage(getKey("parse_error"),
                "<color:red>未能解析<id>");
    }

    public static FormattableMessage morphNotOwnedString()
    {
        return new FormattableMessage(getKey("morph_not_owned"),
                "<color:red>你尚未拥有此伪装");
    }

    public static FormattableMessage disguiseCoolingDownString()
    {
        return new FormattableMessage(getKey("cooling_down"),
                "<color:red>请等一会再进行伪装");
    }

    public static FormattableMessage disguiseNotDefinedString()
    {
        return new FormattableMessage(getKey("disguise_not_defined"),
                "<color:red>未指定伪装");
    }


    public static FormattableMessage headDisguiseDisabledString()
    {
        return new FormattableMessage(getKey("head_morph_disabled"),
                "<color:red>此功能已禁用");
    }

    public static FormattableMessage invalidSkinString()
    {
        return new FormattableMessage(getKey("invalid_skin"),
                "<color:red>无效的皮肤");
    }


    public static FormattableMessage stateRecoverReasonString()
    {
        return new FormattableMessage(getKey("state.recover_reason"),
                "自上次离线后相关功能已被重置");
    }

    public static FormattableMessage recoveringStateString()
    {
        return new FormattableMessage(getKey("state.recovering"),
                "我们正在恢复您的伪装");
    }

    public static FormattableMessage recoveringStateLimitedString()
    {
        return new FormattableMessage(getKey("state.recovering_limited"),
                "我们正从有限副本中恢复您的伪装");
    }

    public static FormattableMessage recoveringStateLimitedHintString()
    {
        return new FormattableMessage(getKey("state.recovering_limited_hint"),
                "<italic>恢复后的伪装可能和之前的不一样");
    }

    public static FormattableMessage recoveringFailedString()
    {
        return new FormattableMessage(getKey("state.recovering_failed"),
                "<color:red>我们无法恢复您的伪装 :(");
    }


    public static FormattableMessage selfVisibleOnString()
    {
        return new FormattableMessage(getKey("self_visible_on"),
                "<color:green>已切换自身可见性");
    }

    public static FormattableMessage selfVisibleOffString()
    {
        return new FormattableMessage(getKey("self_visible_off"),
                "<color:red>已切换自身可见性");
    }

    private static String getKey(String key)
    {
        return "morph." + key;
    }
}
