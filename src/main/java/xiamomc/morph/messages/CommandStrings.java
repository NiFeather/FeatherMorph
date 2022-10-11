package xiamomc.morph.messages;

public class CommandStrings implements IStrings
{
    public static FormattableMessage noPermissionMessage = new FormattableMessage(getKey("no_permission"),
            "<color:red>禁止接触");

    //region mmorph

    //reload
    public static FormattableMessage reloadCompleteMessage = new FormattableMessage(getKey("mmorph.reload_complete"),
            "重载完成！");

    //query
    public static FormattableMessage qDisguisedString = new FormattableMessage(getKey("mmorph.query"),
            "<who>正伪装为<what><storage_status>");

    public static FormattableMessage qDisguisedUnManageableString = new FormattableMessage(getKey("mmorph.query_unmanageable"),
            "<who>正伪装为<what>（无法管理）");

    public static FormattableMessage qNotDisguisedString = new FormattableMessage(getKey("mmorph.query_not_disguised"),
            "<who>没有伪装为任何东西");

    //queryall
    public static FormattableMessage qaNoBodyDisguisingString = new FormattableMessage(getKey("mmorph.queryall_nobody_disguising"),
            "没有人伪装成任何东西");

    public static FormattableMessage qaDisguisedString = new FormattableMessage(getKey("mmorph.queryall_disguising"),
            "<who><status><storage_status> 伪装成了 <what>");

    public static FormattableMessage qaOnlineString = new FormattableMessage(getKey("mmorph.queayall_online"),
            "");

    public static FormattableMessage qaOfflineString = new FormattableMessage(getKey("mmorph.queayall_offline"),
            "（离线）");

    public static FormattableMessage qaIsOfflineStoreString = new FormattableMessage(getKey("mmorph.queryall_offline_store"),
            "（离线存储）");

    public static FormattableMessage qaShowingDisguisedItemsString = new FormattableMessage(getKey("mmorph.queryall_showing_disguised_item"),
            "（显示伪装装备）");

    public static FormattableMessage qaNotShowingDisguisedItemsString = new FormattableMessage(getKey("mmorph.queryall_not_showing_disguised_item"),
            "");

    //manage
    public static FormattableMessage revokeSuccessString = new FormattableMessage(getKey("manage_revoke_success"),
            "<color:green>成功将<what>的伪装从<who>移除");

    public static FormattableMessage revokeFailString = new FormattableMessage(getKey("manage_revoke_fail"),
            "<color:red>未能将<what>的伪装从<who>移除");

    public static FormattableMessage grantSuccessString = new FormattableMessage(getKey("manage_grant_success"),
            "<color:green>成功将<what>的伪装给与<who>");

    public static FormattableMessage grantFailString = new FormattableMessage(getKey("manage_grant_fail"),
            "<color:red>未能将<what>的伪装给与<who>，他是否已经拥有此伪装？");

    public static FormattableMessage unMorphedSomeoneString = new FormattableMessage(getKey("unmorph_someone_success"),
            "成功取消<who>的伪装！");

    public static FormattableMessage unMorphedAllString = new FormattableMessage(getKey("unmorph_all_success"),
            "成功取消所有人的伪装！");

    //chatoverride
    public static FormattableMessage chatOverrideEnabledString = new FormattableMessage(getKey("chat_override_enabled"),
            "聊天覆盖已<bold>启用");

    public static FormattableMessage chatOverrideDisabledString = new FormattableMessage(getKey("chat_override_disabled"),
            "聊天覆盖已<bold>禁用");

    //endregion mmorph

    private static String getKey(String key)
    {
        return "commands." + key;
    }
}
