package xiamomc.morph.messages;

import xiamomc.pluginbase.Messages.FormattableMessage;

public class HintStrings extends AbstractMorphStrings
{
    public static FormattableMessage morphVisibleAfterCommandString()
    {
        return getFormattable(getKey("morph_visible_after_command"),
                "伪装将在切换自身可见后对自己显示");
    }

    public static FormattableMessage skillString()
    {
        return getFormattable(getKey("skill_hint"),
                "小提示: 手持胡萝卜钓竿下蹲使用可以激活当前伪装的主动技能");
    }

    public static FormattableMessage clientSkillString()
    {
        return getFormattable(getKey("skill_hint_client"),
                "小提示：按下<key:key.morphclient.skill>键可以激活当前伪装的主动技能");
    }

    public static FormattableMessage firstGrantHintString()
    {
        return getFormattable(getKey("command_hint"),
                "小提示：发送 /feathermorph help 可以查看伪装的相关用法。");
    }

    public static FormattableMessage firstGrantClientHintString()
    {
        return getFormattable(getKey("command_hint_client"),
                "小提示：按<key:key.morphclient.morph>键可以打开伪装界面。");
    }

    public static FormattableMessage clientSuggestionStringA()
    {
        return getFormattable(getKey("client_suggestion_message_1"),
                "小提示：我们推荐使用MorphClient模组来获得更好的体验");
    }

    public static FormattableMessage clientSuggestionStringB()
    {
        return getFormattable(getKey("client_suggestion_message_2"),
                "小提示：更多信息请询问服务器管理员");
    }

    protected static String getKey(String key)
    {
        return "hint." + key;
    }
}
