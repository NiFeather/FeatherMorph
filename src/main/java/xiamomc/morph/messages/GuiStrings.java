package xiamomc.morph.messages;

import xiamomc.pluginbase.Messages.FormattableMessage;

public class GuiStrings extends AbstractMorphStrings
{
    public static FormattableMessage nextPage()
    {
        return getFormattable(getKey("next_page"), "[Fallback] 下一页");
    }

    public static FormattableMessage prevPage()
    {
        return getFormattable(getKey("prev_page"), "[Fallback] 上一页");
    }

    public static FormattableMessage unDisguise()
    {
        return getFormattable(getKey("undisguise"), "[Fallback] 取消伪装");
    }

    public static FormattableMessage selectDisguise()
    {
        return getFormattable(getKey("title_select_disguise"), "[Fallback] 选择伪装");
    }

    private static String getKey(String key)
{
    return "chestui." + key;
}
}
