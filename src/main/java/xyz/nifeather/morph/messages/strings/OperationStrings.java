package xyz.nifeather.morph.messages.strings;

import xiamomc.pluginbase.Messages.FormattableMessage;

public class OperationStrings extends AbstractMorphStrings
{
    public static FormattableMessage overwritingOneOrMore()
    {
        return getFormattable(getKey("overwriting_one_or_more"), "[Fb] 覆盖一个多更多<type>");
    }

    private static String getKey(String key)
    {
        return "operation." + key;
    }
}
