package xiamomc.morph.messages;

import xiamomc.pluginbase.Messages.FormattableMessage;

public class UpdateStrings extends AbstractMorphStrings
{
    public static FormattableMessage messageHeaderFooter()
    {
        return getFormattable(getKey("msg_header_footer"),
                "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
    }

    public static FormattableMessage checkingUpdate()
    {
        return getFormattable(getKey("checking_update"),
                "检查更新中...");
    }

    public static FormattableMessage newVersionAvailable()
    {
        return getFormattable(getKey("new_version_available"),
                "FeatherMorph有新版本辣！(<current> -> <origin>)");
    }

    public static FormattableMessage noNewVersionAvailable()
    {
        return getFormattable(getKey("no_new_ver_available"),
                "FeatherMorph已是最新版本");
    }

    public static FormattableMessage update_here()
    {
        return getFormattable(getKey("update_here"),
                "在此更新: <url>");
    }

    private static String getKey(String key)
    {
        return "update." + key;
    }
}
