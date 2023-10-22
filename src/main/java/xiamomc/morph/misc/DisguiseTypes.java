package xiamomc.morph.misc;

import org.bukkit.NamespacedKey;

import java.util.Arrays;

/**
 * 伪装类型
 */
public enum DisguiseTypes
{
    /**
     * 原版伪装
     */
    VANILLA(NamespacedKey.MINECRAFT),

    ITEM_DISPLAY("item_display"),

    BLOCK_DISPLAY("block_display"),

    /**
     * 玩家伪装
     */
    PLAYER("player"),

    /**
     * LibsDisguise的本地伪装
     */
    LD("local"),

    /**
     * 外部伪装，可能由其他插件构建
     */
    EXTERNAL("external"),

    /**
     * 未知类型或未指定类型
     */
    UNKNOWN("unknown");

    private final String nameSpace;

    private DisguiseTypes(String namespace)
    {
        this.nameSpace = namespace;
    }

    public String getNameSpace()
    {
        return nameSpace;
    }

    public static DisguiseTypes fromNameSpace(String namespace)
    {
        var types = DisguiseTypes.values();

        var optional = Arrays.stream(types).filter(t -> t.getNameSpace().equals(namespace)).findFirst();

        return optional.orElse(UNKNOWN);
    }

    /**
     * 获取某个ID的伪装类型
     * @param id 目标ID
     * @return 伪装类型，如果找不到Provider则返回null
     * @apiNote minecraft:player不能作为ID传入，请使用player:xxx
     */
    public static DisguiseTypes fromId(String id)
    {
        //将minecraft:player视作外部伪装
        if (id.equals("minecrcaft:player"))
            return DisguiseTypes.EXTERNAL;

        var str = id + ":";
        var idSplited = str.split(":", 3);
        var result = fromNameSpace(idSplited[0]);

        if (result == UNKNOWN)
            result = EXTERNAL;

        return result;
    }

    public static DisguiseTypes fromId(NamespacedKey key)
    {
        return fromId(key.asString());
    }

    public String toId(String id)
    {
        return this.getNameSpace() + ":" + id;
    }

    public NamespacedKey toNamespacedKey(String id)
    {
        return NamespacedKey.fromString(toId(id));
    }

    public String toStrippedId(String rawString)
    {
        return rawString.replace(this.getNameSpace() + ":", "");
    }
}
