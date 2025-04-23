package xyz.nifeather.morph.messages;

import xiamomc.pluginbase.Messages.FormattableMessage;

public class UpdateStrings extends AbstractMorphStrings
{
    public static FormattableMessage messageHeaderFooter()
    {
        return getFormattable(getKey("msg_header_footer"),
                "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
    }

    public static FormattableMessage failed()
    {
        return getFormattable(getKey("check_failed"), "[Fallback] <red>检查失败，请查看控制台查看更多消息");
    }

    public static FormattableMessage currentIsNewer()
    {
        return getFormattable(getKey("current_is_newer"), "[Fallback] <gold>当前版本比<mc_version>的最新版更新...? 可能哪里出问题了...");
    }

    public static FormattableMessage notListed()
    {
        return getFormattable(getKey("current_not_listed"), "[Fallback] <gold>当前版本尚不存在 Release 通道，或者你的服务端 '<software>' 不受支持...");
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
