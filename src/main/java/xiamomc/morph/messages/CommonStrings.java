package xiamomc.morph.messages;

public class CommonStrings
{
    public static FormattableMessage chatOverrideString = new FormattableMessage(getKey("chat.overridePattern"), "<<who>> <message>");

    public static FormattableMessage pluginMessageString = new FormattableMessage(getKey("plugin.messagePattern"), "[Yamp] <message>");

    private static String getKey(String key)
    {
        return "common." + key;
    }
}
