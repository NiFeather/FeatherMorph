package xiamomc.morph.misc;

import org.bukkit.NamespacedKey;

import java.util.Arrays;

public enum DisguiseTypes
{
    VANILLA(NamespacedKey.MINECRAFT),
    PLAYER("player"),
    LD("ld"),
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

    public static DisguiseTypes fromId(String id)
    {
        var idSplited = id.split(":");

        if (id.equals("minecrcaft:player"))
            throw new IllegalArgumentException("minecraft:player不能当作id传入，请使用player:xxx");

        if (idSplited.length < 1) return UNKNOWN;
        else return fromNameSpace(idSplited[0]);
    }

    public String toId(String id)
    {
        return this.getNameSpace() + ":" + id;
    }

    public String toStrippedId(String rawString)
    {
        return rawString.replace(this.getNameSpace() + ":", "");
    }
}
