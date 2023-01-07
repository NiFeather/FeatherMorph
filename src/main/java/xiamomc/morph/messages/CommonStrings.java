package xiamomc.morph.messages;

import xiamomc.pluginbase.Messages.FormattableMessage;

public class CommonStrings extends AbstractMorphStrings
{
    public static FormattableMessage chatOverrideString()
    {
        return getFormattable(getKey("chat.overridePattern"),
                "<<who>> <message>");
    }

    public static FormattableMessage pluginMessageString()
    {
        return getFormattable(getKey("plugin.messagePattern"),
                "<color:#dddddd>≡ FeatherMorph » <message>");
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
