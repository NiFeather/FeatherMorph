package xiamomc.morph.messages;

import xiamomc.pluginbase.messages.FormattableMessage;

public class RequestStrings extends AbstractMorphStrings
{
    public static FormattableMessage requestSendString()
    {
        return getFormattable(getKey("request_send"),
                "请求已发送！对方将有有1分钟的时间来接受！");
    }

    public static FormattableMessage requestAlreadySentString()
    {
        return getFormattable(getKey("request_already_sent"),
                "你已经向<who>发过一个请求了");
    }

    public static FormattableMessage requestReceivedString()
    {
        return getFormattable(getKey("request_received"),
                "你收到了来自<who>的请求！");
    }


    /**
     * 发起方的接受消息
     */
    public static FormattableMessage targetAcceptedString()
    {
        return getFormattable(getKey("request_accepted_target"),
                "发往<who>的请求已接受！");
    }

    /**
     * 接受方的接受消息
     */
    public static FormattableMessage sourceAcceptedString()
    {
        return getFormattable(getKey("request_accepted"),
                "来自<who>的请求已接受！");
    }


    public static FormattableMessage targetDeniedString()
    {
        return getFormattable(getKey("request_denied_target"),
                "发往<who>的请求已拒绝！");
    }

    public static FormattableMessage sourceDeniedString()
    {
        return getFormattable(getKey("request_denied"),
                "来自<who>的请求已拒绝！");
    }


    public static FormattableMessage cantSendToSelfString()
    {
        return getFormattable(getKey("cant_send_to_self"),
                "<color:red>你不能给自己发请求");
    }

    public static FormattableMessage alreadyHaveDisguiseString()
    {
        return getFormattable(getKey("already_have_disguise"),
                "<color:red>你已经有对方的形态了");
    }

    public static FormattableMessage requestNotFound()
    {
        return getFormattable(getKey("request_not_found"),
                "未找到目标请求，可能已经过期？");
    }

    private static String getKey(String key)
    {
        return "requests." + key;
    }
}
