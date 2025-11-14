package xyz.nifeather.morph.messages.strings;

import xiamomc.pluginbase.Messages.FormattableMessage;

public class ExceptionStrings extends AbstractMorphStrings
{
    public static FormattableMessage noUserInput()
    {
        return getFormattable(getKey("no_user_input"), "[Fallback] 这该死的属性不接受任何输入");
    }

    public static FormattableMessage unsupported()
    {
        return getFormattable(getKey("unsupported"), "[Fallback] 该属性对此形态不可用");
    }

    public static FormattableMessage internalProperty()
    {
        return getFormattable(getKey("internal_property"), "[Fallback] 内部属性，不接受用户输入");
    }

    public static FormattableMessage noEmptyInput()
    {
        return getFormattable(getKey("no_empty_input"), "[Fallback] 该属性不支持空输入");
    }

    public static FormattableMessage failedParsingWhatFromInput()
    {
        return getFormattable(getKey("failed_parsing_what_from_input"), "[Fallback] 无法将输入解析为 <type>");
    }

    public static FormattableMessage emptyValueCandidate()
    {
        return getFormattable(getKey("empty_value_candidate"), "[Fallback] 没有可用候选！服务器程序是否完整？");
    }

    public static FormattableMessage noValueMatch()
    {
        return getFormattable(getKey("no_value_match"), "[Fallback] 没有与输入匹配的值");
    }

    public static FormattableMessage registryNotAvailable()
    {
        return getFormattable(getKey("registry_not_available"), "[Fallback] 注册表不可用！服务器程序是否完整？");
    }

    public static FormattableMessage nonFinite()
    {
        return getFormattable(getKey("non_finite_input"), "[Fallback] 该属性不接受非有限输入");
    }

    public static FormattableMessage outOfRangeClosedBracket()
    {
        return getFormattable(getKey("input_out_of_range_closed_bracket"), "[Fallback] 输入值超过了 [<min>, <max>] 的范围限定");
    }

    public static FormattableMessage inputTooLong()
    {
        return getFormattable(getKey("input_too_long"), "[Fallback] 输入过长");
    }

    public static FormattableMessage inputTooMany()
    {
        return getFormattable(getKey("too_many_elements"), "[Fallback] 输入包含的元素过多！该属性最多接受<max>个元素");
    }

    public static FormattableMessage inputNotAllowed()
    {
        return getFormattable(getKey("input_not_allowed"), "[Fallback] 不接受此输入");
    }

    public static FormattableMessage malformedInput()
    {
        return getFormattable(getKey("malformed_input"), "[Fallback] 不正确的格式");
    }

    private static String getKey(String key)
    {
        return "exceptions." + key;
    }
}
