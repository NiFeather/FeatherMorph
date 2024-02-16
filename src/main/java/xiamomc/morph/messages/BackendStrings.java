package xiamomc.morph.messages;

import xiamomc.pluginbase.Messages.FormattableMessage;

public class BackendStrings extends AbstractMorphStrings
{
    public static FormattableMessage noSuchBackend()
    {
        return getFormattable(getKey("no_such_backend"), "<color:red>无此后端");
    }

    public static FormattableMessage switchSuccess()
    {
        return getFormattable(getKey("switch_success"), "已切换后端为<name>！");
    }

    public static FormattableMessage switchFailed()
    {
        return getFormattable(getKey("switch_fail"), "<color:red>切换后端时发生异常");
    }

    public static FormattableMessage experimentalWarning()
    {
        return getFormattable(getKey("experiemental_warning"), "请注意此功能正在实验，并可能在未来移除！");
    }

    public static FormattableMessage nilBackendName()
    {
        return getFormattable(getKey("name.nil"), "后备（客户端）渲染器");
    }

    public static FormattableMessage serverBackendName()
    {
        return getFormattable(getKey("name.server"), "服务端渲染器");
    }

    private static String getKey(String key)
    {
        return "backend." + key;
    }
}
