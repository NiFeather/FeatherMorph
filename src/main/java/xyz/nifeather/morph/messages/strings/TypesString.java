package xyz.nifeather.morph.messages.strings;

import xiamomc.pluginbase.Messages.FormattableMessage;

public class TypesString extends AbstractMorphStrings
{
    public static FormattableMessage typeInteger()
    {
        return getFormattable(getKey("int"), "整数");
    }

    public static FormattableMessage typeFloat()
    {
        return getFormattable(getKey("float"), "单精度浮点");
    }

    public static FormattableMessage typeDouble()
    {
        return getFormattable(getKey("double"), "双精度浮点");
    }

    public static FormattableMessage typeString()
    {
        return getFormattable(getKey("string"), "字符串");
    }

    public static FormattableMessage resourceLocation()
    {
        return getFormattable(getKey("resource_location"), "命名空间ID");
    }

    public static FormattableMessage collectionOf()
    {
        return getFormattable(getKey("collection_of"), "<what>的集合");
    }

    public static FormattableMessage textComponent()
    {
        return getFormattable(getKey("text_component"), "文本组件");
    }

    public static FormattableMessage localeFile()
    {
        return getFormattable(getKey("locale_file"), "[Fallback] 语言文件");
    }

    private static String getKey(String key)
    {
        return "types." + key;
    }
}
