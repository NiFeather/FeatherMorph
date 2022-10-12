package xiamomc.morph.messages;

import xiamomc.pluginbase.messages.FormattableMessage;
import xiamomc.pluginbase.messages.IStrings;

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


    public static FormattableMessage morphDescription()
    {
        return new FormattableMessage(getKey("description_morph"),
                "伪装为某个玩家或某种生物");
    }

    public static FormattableMessage unMorphDescription()
    {
        return new FormattableMessage(getKey("description_unmorph"),
                "取消伪装");
    }

    public static FormattableMessage morphPlayerDescription()
    {
        return new FormattableMessage(getKey("description_morphplayer"),
                "伪装为某个玩家（等同于 /morph player:<玩家名>）");
    }

    public static FormattableMessage mmorphDescription()
    {
        return new FormattableMessage(getKey("description_mmorph"),
                "插件指令");
    }

    public static FormattableMessage requestDescription()
    {
        return new FormattableMessage(getKey("description_request"),
                "管理交换请求");
    }

    public static FormattableMessage requestDenyDescription()
    {
        return new FormattableMessage(getKey("description_request_deny"),
                "拒绝交换请求");
    }

    public static FormattableMessage requestAcceptDescription()
    {
        return new FormattableMessage(getKey("description_request_accept"),
                "接受交换请求");
    }

    public static FormattableMessage requestSendDescription()
    {
        return new FormattableMessage(getKey("description_request_send"),
                "发送交换请求");
    }

    public static FormattableMessage requestDescriptionSpecialNote()
    {
        return new FormattableMessage(getKey("description_request_note"),
                "交换请求接受后双方都可以变成对方的样子");
    }



    public static FormattableMessage reloadDescription()
    {
        return new FormattableMessage(getKey("description_reload"),
                "重载插件配置");
    }

    public static FormattableMessage toggleSelfDescription()
    {
        return new FormattableMessage(getKey("description_toggle_self"),
                "切换自身可见性");
    }

    public static FormattableMessage queryDescription()
    {
        return new FormattableMessage(getKey("description_query"),
                "查询某个玩家的伪装状态");
    }

    public static FormattableMessage queryAllDescription()
    {
        return new FormattableMessage(getKey("description_queryall"),
                "查询所有已注册的伪装状态");
    }

    public static FormattableMessage helpDescription()
    {
        return new FormattableMessage(getKey("description_help"),
                "显示帮助信息");
    }

    public static FormattableMessage manageDescription()
    {
        return new FormattableMessage(getKey("description_manage"),
                "管理伪装");
    }

    public static FormattableMessage manageRevokeDescription()
    {
        return new FormattableMessage(getKey("description_manage_revoke"),
                "剥夺某人某个伪装");
    }

    public static FormattableMessage manageGrantDescription()
    {
        return new FormattableMessage(getKey("description_manage_grant"),
                "给予某人某个伪装");
    }

    public static FormattableMessage manageUnmorphDescription()
    {
        return new FormattableMessage(getKey("description_manage_unmorph"),
                "强制取消某人的伪装");
    }

    public static FormattableMessage chatOverrideDescription()
    {
        return new FormattableMessage(getKey("description_chatoverride"),
                "查看服务器的聊天覆盖状态");
    }

    public static FormattableMessage chatOverrideQueryDescription()
    {
        return new FormattableMessage(getKey("description_chatoverride_query"),
                "查询当前覆盖状态");
    }

    public static FormattableMessage chatOverrideToggleDescription()
    {
        return new FormattableMessage(getKey("description_chatoverride_toggle"),
                "切换当前覆盖状态");
    }

    private static String getKey(String key)
    {
        return "help." + key;
    }
}
