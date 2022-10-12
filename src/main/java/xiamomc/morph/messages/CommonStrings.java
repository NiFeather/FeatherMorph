package xiamomc.morph.messages;

import xiamomc.pluginbase.messages.FormattableMessage;
import xiamomc.pluginbase.messages.IStrings;

public class CommonStrings implements IStrings
{
    public static FormattableMessage chatOverrideString()
    {
        return new FormattableMessage(getKey("chat.overridePattern"),
                "<<who>> <message>");
    }

    public static FormattableMessage pluginMessageString()
    {
        return new FormattableMessage(getKey("plugin.messagePattern"),
                "[Yamp] <message>");
    }

    public static FormattableMessage playerNotFoundString()
    {
        return new FormattableMessage(getKey("player_not_found"),
                "<color:red>未找到目标玩家或对方已离线");
    }

    public static FormattableMessage playerNotDefinedString()
    {
        return new FormattableMessage(getKey("player_not_defined"),
                "<color:red>未指定玩家");
    }

    public static FormattableMessage commandNotFoundString()
    {
        return new FormattableMessage(getKey("command_not_found"),
                "<color:red>未找到此指令");
    }

    private static String getKey(String key)
    {
        return "common." + key;
    }
}
