package xiamomc.morph.messages;

public class HelpStrings implements IStrings
{
    //help
    public static FormattableMessage avaliableCommandHeaderString()
    {
        return new FormattableMessage(getKey("avaliable_cmd_header"),
                "当前可用的指令（单击补全/查看）");
    }

    public static FormattableMessage commandNamePatternString()
    {
        return new FormattableMessage(getKey("cmdname_pattern"),
                "/<basename>... -- <description>");
    };

    public static FormattableMessage clickToCompleteString()
    {
        return new FormattableMessage(getKey("click_to_complete"),
                "点击补全");
    }

    public static FormattableMessage clickToViewString()
    {
        return new FormattableMessage(getKey("click_to_view"),
                "点击查看");
    }

    public static FormattableMessage commandSectionHeaderString()
    {
        return new FormattableMessage(getKey("section_header"),
                "指令 /<basename> 的用法：");
    };

    public static FormattableMessage commandEntryString()
    {
        return new FormattableMessage(getKey("cmd_entry"),
            "/<basename>：<description>");
    }

    public static FormattableMessage specialNoteString()
    {
        return new FormattableMessage(getKey("special_note"),
                "特别标注：");
    };

    public static FormattableMessage morphCommandDescription()
    {
        return new FormattableMessage(getKey("morph_command_description"),
                "伪装、取消伪装");
    }

    public static FormattableMessage morphCommandSpecialNote1()
    {
        return new FormattableMessage(getKey("morph_command_note_1"),
                "伪装可以通过击杀生物或玩家获得");
    }

    public static FormattableMessage morphCommandSpecialNote2()
    {
        return new FormattableMessage(getKey("morph_command_note_2"),
                "伪装时会优先复制视线方向5格以内的相同生物或玩家进行伪装");
    }

    private static String getKey(String key)
    {
        return "help." + key;
    }
}
