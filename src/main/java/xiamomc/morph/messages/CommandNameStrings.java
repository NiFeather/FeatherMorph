package xiamomc.morph.messages;

import xiamomc.pluginbase.Messages.FormattableMessage;

public class CommandNameStrings extends AbstractMorphStrings
{
    public static FormattableMessage bossbarString()
    {
        return getFormattable(getKey("bossbar"), "Bossbar模拟");
    }

    public static FormattableMessage allowLDDisguiseString()
    {
        return getFormattable(getKey("allow_ld"), "LibsDisguise自定义伪装支持");
    }

    public static FormattableMessage headMorphString()
    {
        return getFormattable(getKey("head_morph"), "头颅快速伪装");
    }

    public static FormattableMessage chatOverrideString()
    {
        return getFormattable(getKey("chatoverride"), "聊天覆盖");
    }

    public static FormattableMessage reverseInteractionString()
    {
        return getFormattable(getKey("reverse_interaction"), "交互控制");
    }

    public static FormattableMessage reverseSneakString()
    {
        return getFormattable(getKey("reverse_sneak"), "潜行控制");
    }

    public static FormattableMessage reverseSwapHandString()
    {
        return getFormattable(getKey("reverse_swaphand"), "副手交换控制");
    }

    public static FormattableMessage reverseDropString()
    {
        return getFormattable(getKey("reverse_drop"), "丢弃控制");
    }

    public static FormattableMessage reverseHotbar()
    {
        return getFormattable(getKey("reverse_hotbar"), "快捷栏控制");
    }

    public static FormattableMessage reverseIgnoreDisguised()
    {
        return getFormattable(getKey("reverse_ignore_disguised"), "使反向控制忽略已伪装的目标");
    }

    private static String getKey(String key)
    {
        return "commands.name." + key;
    }
}
