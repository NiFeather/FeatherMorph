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
        return getFormattable(getKey("allow_ld"), "本地伪装支持");
    }

    public static FormattableMessage headMorphString()
    {
        return getFormattable(getKey("head_morph"), "头颅伪装");
    }

    public static FormattableMessage chatOverrideString()
    {
        return getFormattable(getKey("chatoverride"), "聊天覆盖");
    }

    public static FormattableMessage mirrorInteractionString()
    {
        return getFormattable(getKey("mirror_interaction"), "交互控制");
    }

    public static FormattableMessage mirrorSneakString()
    {
        return getFormattable(getKey("mirror_sneak"), "潜行控制");
    }

    public static FormattableMessage mirrorSwapHandString()
    {
        return getFormattable(getKey("mirror_swaphand"), "副手交换控制");
    }

    public static FormattableMessage mirrorDropString()
    {
        return getFormattable(getKey("mirror_drop"), "丢弃控制");
    }

    public static FormattableMessage mirrorHotbar()
    {
        return getFormattable(getKey("mirror_hotbar"), "快捷栏控制");
    }

    public static FormattableMessage mirrorIgnoreDisguised()
    {
        return getFormattable(getKey("mirror_ignore_disguised"), "使反向控制忽略已伪装的目标");
    }

    private static String getKey(String key)
    {
        return "commands.option.name." + key;
    }
}
