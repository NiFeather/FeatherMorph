package xyz.nifeather.morph.messages.strings;

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

    public static FormattableMessage requestingRemote()
    {
        return getFormattable(getKey("requesting_remote"),
                "[Fallback] 正在发送请求至远程服务");
    }

    public static FormattableMessage dataNotLoaded()
    {
        return getFormattable(getKey("data_not_loaded"),
                "[Fallback] <what> 尚未加载");
    }

    public static FormattableMessage on()
    {
        return getFormattable(getKey("on"), "[Fallback] ON");
    }

    public static FormattableMessage off()
    {
        return getFormattable(getKey("off"), "[Fallback] OFF");
    }

    private static String getKey(String key)
    {
        return "common." + key;
    }
}
