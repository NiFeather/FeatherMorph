package xiamomc.morph.messages;

public class CommandStrings implements IStrings
{
    public static FormattableMessage noPermissionMessage()
    {
        return new FormattableMessage(getKey("no_permission"),
                "<color:red>禁止接触");
    };

    //reload
    public static FormattableMessage reloadCompleteMessage()
    {
        return new FormattableMessage(getKey("reload_complete"),
                "重载完成！");
    };

    //query
    public static FormattableMessage qDisguisedString()
    {
        return new FormattableMessage(getKey("query"),
                "<who>正伪装为<what><storage_status>");
    }

    public static FormattableMessage qDisguisedUnManageableString()
    {
        return new FormattableMessage(getKey("query_unmanageable"),
                "<who>正伪装为<what>（无法管理）");
    }

    public static FormattableMessage qNotDisguisedString()
    {
        return new FormattableMessage(getKey("query_not_disguised"),
                "<who>没有伪装为任何东西");
    }

    //queryall
    public static FormattableMessage qaNoBodyDisguisingString()
    {
        return new FormattableMessage(getKey("queryall_nobody_disguising"),
                "没有人伪装成任何东西");
    }

    public static FormattableMessage qaDisguisedString()
    {
        return new FormattableMessage(getKey("queryall_disguising"),
                "<who><status><storage_status> 伪装成了 <what>");
    }

    public static FormattableMessage qaOnlineString()
    {
        return new FormattableMessage(getKey("queayall_online"),
                "");
    }

    public static FormattableMessage qaOfflineString()
    {
        return new FormattableMessage(getKey("queayall_offline"),
                "（离线）");
    }

    public static FormattableMessage qaIsOfflineStoreString()
    {
        return new FormattableMessage(getKey("queryall_offline_store"),
                "（离线存储）");
    }

    public static FormattableMessage qaShowingDisguisedItemsString()
    {
        return new FormattableMessage(getKey("queryall_showing_disguised_item"),
                "（显示伪装装备）");
    }

    public static FormattableMessage qaNotShowingDisguisedItemsString()
    {
        return new FormattableMessage(getKey("queryall_not_showing_disguised_item"),
                "");
    }

    //manage
    public static FormattableMessage revokeSuccessString()
    {
        return new FormattableMessage(getKey("manage_revoke_success"),
                "<color:green>成功将<what>的伪装从<who>移除");
    }

    public static FormattableMessage revokeFailString()
    {
        return new FormattableMessage(getKey("manage_revoke_fail"),
                "<color:red>未能将<what>的伪装从<who>移除");
    }

    public static FormattableMessage grantSuccessString()
    {
        return new FormattableMessage(getKey("manage_grant_success"),
                "<color:green>成功将<what>的伪装给与<who>");
    }

    public static FormattableMessage grantFailString()
    {
        return new FormattableMessage(getKey("manage_grant_fail"),
                "<color:red>未能将<what>的伪装给与<who>，他是否已经拥有此伪装？");
    }

    public static FormattableMessage unMorphedSomeoneString()
    {
        return new FormattableMessage(getKey("unmorph_someone_success"),
                "成功取消<who>的伪装！");
    }

    public static FormattableMessage unMorphedAllString()
    {
        return new FormattableMessage(getKey("unmorph_all_success"),
                "成功取消所有人的伪装！");
    }

    //chatoverride
    public static FormattableMessage chatOverrideEnabledString()
    {
        return new FormattableMessage(getKey("chat_override_enabled"),
                "聊天覆盖已<bold>启用");
    }

    public static FormattableMessage chatOverrideDisabledString()
    {
        return new FormattableMessage(getKey("chat_override_disabled"),
                "聊天覆盖已<bold>禁用");
    }

    private static String getKey(String key)
    {
        return "commands." + key;
    }
}
