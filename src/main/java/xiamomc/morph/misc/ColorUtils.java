package xiamomc.morph.misc;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.util.WeakHashMap;

public class ColorUtils
{
    private static final WeakHashMap<TextColor, ChatColor> colorWeakHashMap = new WeakHashMap<>();

    @Nullable
    public static ChatColor toChatColor(@Nullable TextColor rawColor)
    {
        if (rawColor == null) return null;

        var sourceColor = NamedTextColor.nearestTo(rawColor);

        ChatColor val = colorWeakHashMap.get(rawColor);

        if (val != null) return val;

        if (compare(NamedTextColor.BLACK, sourceColor)) val = ChatColor.BLACK;
        if (compare(NamedTextColor.DARK_BLUE, sourceColor)) val = ChatColor.DARK_BLUE;
        if (compare(NamedTextColor.DARK_GREEN, sourceColor)) val = ChatColor.DARK_GREEN;
        if (compare(NamedTextColor.DARK_AQUA, sourceColor)) val = ChatColor.DARK_AQUA;
        if (compare(NamedTextColor.DARK_RED, sourceColor)) val = ChatColor.DARK_RED;
        if (compare(NamedTextColor.DARK_PURPLE, sourceColor)) val = ChatColor.DARK_PURPLE;
        if (compare(NamedTextColor.GOLD, sourceColor)) val = ChatColor.GOLD;
        if (compare(NamedTextColor.GRAY, sourceColor)) val = ChatColor.GRAY;
        if (compare(NamedTextColor.DARK_GRAY, sourceColor)) val = ChatColor.DARK_GRAY;
        if (compare(NamedTextColor.BLUE, sourceColor)) val = ChatColor.BLUE;
        if (compare(NamedTextColor.GREEN, sourceColor)) val = ChatColor.GREEN;
        if (compare(NamedTextColor.AQUA, sourceColor)) val = ChatColor.AQUA;
        if (compare(NamedTextColor.RED, sourceColor)) val = ChatColor.RED;
        if (compare(NamedTextColor.LIGHT_PURPLE, sourceColor)) val = ChatColor.LIGHT_PURPLE;
        if (compare(NamedTextColor.YELLOW, sourceColor)) val = ChatColor.YELLOW;
        if (compare(NamedTextColor.WHITE, sourceColor)) val = ChatColor.WHITE;

        colorWeakHashMap.put(rawColor, val);

        if (val == null)
            throw new IllegalArgumentException("找不到和" + rawColor.asHexString() + "匹配的ChatColor");

        return val;
    }

    @Nullable
    public static TextColor fromChatColor(@Nullable ChatColor rawColor)
    {
        if (rawColor == null) return null;

        return switch (rawColor)
        {
            case BLACK -> NamedTextColor.BLACK;
            case DARK_BLUE -> NamedTextColor.DARK_BLUE;
            case DARK_GREEN -> NamedTextColor.DARK_GREEN;
            case DARK_AQUA -> NamedTextColor.DARK_AQUA;
            case DARK_RED -> NamedTextColor.DARK_RED;
            case DARK_PURPLE -> NamedTextColor.DARK_PURPLE;
            case GOLD -> NamedTextColor.GOLD;
            case GRAY -> NamedTextColor.GRAY;
            case DARK_GRAY -> NamedTextColor.DARK_GRAY;
            case BLUE -> NamedTextColor.BLUE;
            case GREEN -> NamedTextColor.GREEN;
            case AQUA  -> NamedTextColor.AQUA;
            case RED -> NamedTextColor.RED;
            case LIGHT_PURPLE -> NamedTextColor.LIGHT_PURPLE;
            case YELLOW -> NamedTextColor.YELLOW;
            default -> NamedTextColor.WHITE;
        };
    }

    private static boolean compare(NamedTextColor source, TextColor target)
    {
        return source.asHexString().equals(target.asHexString());
    }
}
