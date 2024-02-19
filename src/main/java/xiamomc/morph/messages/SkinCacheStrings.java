package xiamomc.morph.messages;

import xiamomc.pluginbase.Messages.FormattableMessage;

public class SkinCacheStrings extends AbstractMorphStrings
{
    public static FormattableMessage listHeader()
    {
        return getFormattable(getKey("list_header"), "当前总共缓存了<count>个皮肤：");
    }

    public static FormattableMessage skinExpired()
    {
        return getFormattable(getKey("skin_expired"), "<color:gold>（已过期）");
    }

    public static FormattableMessage andXMore()
    {
        return getFormattable(getKey("and_x_more"), "...以及其他<count>个皮肤");
    }

    public static FormattableMessage skinInfoOverallLine()
    {
        return getFormattable(getKey("skin_info_overall"), "<info_line> <x_more>");
    }

    public static FormattableMessage droppedAllSkins()
    {
        return getFormattable(getKey("dropped_all_skins"), "已清除<count>个皮肤");
    }

    public static FormattableMessage droppedSkin()
    {
        return getFormattable(getKey("dropped_skin"), "已清除<name>的皮肤");
    }

    public static FormattableMessage fetchingSkin()
    {
        return getFormattable(getKey("fetching_skin"), "正在尝试获取<name>的皮肤");
    }

    public static FormattableMessage fetchSkinSuccess()
    {
        return getFormattable(getKey("fetch_skin_success"), "成功获取<name>的皮肤！");
    }

    public static FormattableMessage targetSkinNotFound()
    {
        return getFormattable(getKey("target_skin_not_found"), "<color:red>查无此人");
    }

    public static FormattableMessage tabFilterPlayerNameHere()
    {
        return getFormattable(getKey("player_name_here"), "<玩家名>");
    }

    public static FormattableMessage infoLine()
    {
        return getFormattable(getKey("info_general"), "<name>的皮肤信息:");
    }

    public static FormattableMessage infoSkinLine()
    {
        return getFormattable(getKey("info_skin"), "皮肤URL: <url>");
    }

    public static FormattableMessage infoCapeLine()
    {
        return getFormattable(getKey("info_cape"), "披风: <cape>");
    }

    public static FormattableMessage copySuccess()
    {
        return getFormattable(getKey("copy_success"), "成功将 <source> 复制到 <target>");
    }

    public static FormattableMessage moveSuccess()
    {
        return getFormattable(getKey("move_success"), "成功将 <source> 移动到 <target>");
    }

    public static FormattableMessage copyMoveTargetExists()
    {
        return getFormattable(getKey("copymove_target_exists"), "<color:red>目标已存在！将不会进行操作...");
    }

    private static String getKey(String key)
    {
        return "commands.skin_cache." + key;
    }
}
