package xiamomc.morph.messages;

public class CommonStrings implements IStrings
{
    public static FormattableMessage chatOverrideString = new FormattableMessage(getKey("chat.overridePattern"),
            "<<who>> <message>");

    public static FormattableMessage pluginMessageString = new FormattableMessage(getKey("plugin.messagePattern"),
            "[Yamp] <message>");

    public static FormattableMessage playerNotFoundString = new FormattableMessage(getKey("player_not_found"),
            "<color:red>未找到目标玩家或对方已离线");

    public static FormattableMessage playerNotDefinedString = new FormattableMessage(getKey("player_not_defined"),
            "<color:red>未指定玩家");

    public static FormattableMessage commandNotFoundString = new FormattableMessage(getKey("command_not_found"),
            "<color:red>未找到此指令");

    private static String getKey(String key)
    {
        return "common." + key;
    }
}
