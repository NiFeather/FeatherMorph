package xyz.nifeather.morph.messages;

import xiamomc.pluginbase.Messages.FormattableMessage;

public class CommonStrings extends AbstractMorphStrings
{
    @Deprecated
    public static FormattableMessage chatOverrideDefaultPattern()
    {
        return getFormattable(getKey("chat.overrideDefaultPattern"),
                "~DEPRECATED, SEE CONFIG.YML");
    }

    @Deprecated
    public static FormattableMessage pluginMessageString()
    {
        return getFormattable(getKey("plugin.messagePattern"),
                "~DEPRECATED, SEE CONFIG.YML");
    }

    public static FormattableMessage playerNotFoundString()
    {
        return getFormattable(getKey("player_not_found"),
                "<color:red>未找到目标玩家或对方已离线");
    }

    public static FormattableMessage playerNotDefinedString()
    {
        return getFormattable(getKey("player_not_defined"),
                "<color:red>未指定玩家");
    }

    public static FormattableMessage commandNotFoundString()
    {
        return getFormattable(getKey("command_not_found"),
                "<color:red>未找到此指令");
    }

    private static String getKey(String key)
    {
        return "common." + key;
    }
}
