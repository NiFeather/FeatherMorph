package xiamomc.morph.messages;

import xiamomc.pluginbase.Messages.FormattableMessage;

public class CommandStrings extends AbstractMorphStrings
{
    public static FormattableMessage noPermissionMessage()
    {
        return getFormattable(getKey("no_permission"),
                "<color:red>禁止接触");
    }

    //reload
    public static FormattableMessage reloadCompleteMessage()
    {
        return getFormattable(getKey("reload_complete"),
                "重载完成！");
    }

    //query
    public static FormattableMessage qDisguisedString()
    {
        return getFormattable(getKey("query"),
                "<who>正伪装为<what><storage_status>");
    }

    public static FormattableMessage qDisguisedUnManageableString()
    {
        return getFormattable(getKey("query_unmanageable"),
                "<who>正伪装为<what>（无法管理）");
    }

    public static FormattableMessage qNotDisguisedString()
    {
        return getFormattable(getKey("query_not_disguised"),
                "<who>没有伪装为任何东西");
    }

    //queryall
    public static FormattableMessage qaNoBodyDisguisingString()
    {
        return getFormattable(getKey("queryall_nobody_disguising"),
                "没有人伪装成任何东西");
    }

    public static FormattableMessage qaDisguisedString()
    {
        return getFormattable(getKey("queryall_disguising"),
                "<who><status><storage_status> 伪装成了 <what>");
    }

    public static FormattableMessage qaOnlineString()
    {
        return getFormattable(getKey("queayall_online"),
                "");
    }

    public static FormattableMessage qaOfflineString()
    {
        return getFormattable(getKey("queayall_offline"),
                "（离线）");
    }

    public static FormattableMessage qaIsOfflineStoreString()
    {
        return getFormattable(getKey("queryall_offline_store"),
                "（离线存储）");
    }

    public static FormattableMessage qaShowingDisguisedItemsString()
    {
        return getFormattable(getKey("queryall_showing_disguised_item"),
                "（显示伪装装备）");
    }

    public static FormattableMessage qaNotShowingDisguisedItemsString()
    {
        return getFormattable(getKey("queryall_not_showing_disguised_item"),
                "");
    }

    //manage
    public static FormattableMessage revokeSuccessString()
    {
        return getFormattable(getKey("manage_revoke_success"),
                "<color:green>成功将<what>的伪装从<who>移除");
    }

    public static FormattableMessage revokeFailString()
    {
        return getFormattable(getKey("manage_revoke_fail"),
                "<color:red>未能将<what>的伪装从<who>移除");
    }

    public static FormattableMessage grantSuccessString()
    {
        return getFormattable(getKey("manage_grant_success"),
                "<color:green>成功将<what>的伪装给与<who>");
    }

    public static FormattableMessage grantFailString()
    {
        return getFormattable(getKey("manage_grant_fail"),
                "<color:red>未能将<what>的伪装给与<who>，他是否已经拥有此伪装？");
    }

    public static FormattableMessage morphedSomeoneString()
    {
        return getFormattable(getKey("morph_someone_success"),
                "成功将<who>伪装为<what>！");
    }

    public static FormattableMessage unMorphedSomeoneString()
    {
        return getFormattable(getKey("unmorph_someone_success"),
                "成功取消<who>的伪装！");
    }

    public static FormattableMessage unMorphedAllString()
    {
        return getFormattable(getKey("unmorph_all_success"),
                "成功取消所有人的伪装！");
    }

    //options
    public static FormattableMessage optionSetString()
    {
        return getFormattable(getKey("option_set"),
                "已将选项<what>设置为<value>");
    }

    public static FormattableMessage optionValueString()
    {
        return getFormattable(getKey("option_get"),
                "<what>已设置为<value>");
    }

    public static FormattableMessage lookupFilterCommand()
    {
        return getFormattable(getKey("lookup_filter"),
                "根据筛选条件找到了以下伪装：");
    }

    //region Illegal arguments

    public static FormattableMessage illegalArgumentString()
    {
        return getFormattable(getKey("illegal_argument"),
                "无效的参数: <detail>");
    }

    public static FormattableMessage argumentTypeErrorString()
    {
        return getFormattable(getKey("illegal_argument.type_error"),
                "参数类型应为<type>");
    }

    //endregion Illegal arguments

    //region Lists

    public static FormattableMessage listNoEnoughArguments()
    {
        return getFormattable(getKey("not_enough_arguments"),
                "参数不足");
    }

    public static FormattableMessage listAddSuccess()
    {
        return getFormattable(getKey("list_add_success"),
                "成功添加<value>到<option>");
    }

    public static FormattableMessage listAddFailUnknown()
    {
        return getFormattable(getKey("list_add_fail_unknown"),
                "未能添加<value>到<option>，可能是类型不对");
    }

    public static FormattableMessage listRemoveSuccess()
    {
        return getFormattable(getKey("list_remove_success"),
                "成功从<option>移除<value>");
    }

    public static FormattableMessage listRemoveFailUnknown()
    {
        return getFormattable(getKey("list_remove_fail_unknown"),
                "未能移除<value>，可能是其不在列表中");
    }

    public static FormattableMessage unknownOperation()
    {
        return getFormattable(getKey("unknown_operation"),
                "未知操作：<operation>");
    }

    //endregion Lists

    //region AI Option

    public static FormattableMessage aiWarningPrimary()
    {
        return getFormattable(getKey("ai_warning_1"),
                "<red>检测到更改了是否修改生物AI");
    }

    public static FormattableMessage aiWarningSecondary()
    {
        return getFormattable(getKey("ai_warning_2"),
                "<red>这需要重启服务器！");
    }

    //endregion Ai Option

    public static FormattableMessage notDisguised()
    {
        return getFormattable(getKey("not_disguised"),
                "<red>你没有进行伪装");
    }

    public static FormattableMessage noSuchAnimation()
    {
        return getFormattable(getKey("no_such_animation"),
                "<red>此动画不可用");
    }

    public static FormattableMessage goingToPlayAnimation()
    {
        return getFormattable(getKey("going_to_play_animation"),
                "即将播放动画 <italic><what></italic>");
    }

    public static FormattableMessage grantItemSuccess()
    {
        return getFormattable(getKey("grant_item_success"), "[Fallback] 成功给与物品。如果没有请检查是否背包已满");
    }

    private static String getKey(String key)
    {
        return "commands." + key;
    }
}
