package xiamomc.morph.misc;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ColorUtils
{
    private final static Map<NamedTextColor, ChatColor> namedToChatMap = new Object2ObjectOpenHashMap<>();

    static
    {
        namedToChatMap.put(NamedTextColor.BLACK, ChatColor.BLACK);
        namedToChatMap.put(NamedTextColor.DARK_BLUE, ChatColor.DARK_BLUE);
        namedToChatMap.put(NamedTextColor.DARK_GREEN, ChatColor.DARK_GREEN);
        namedToChatMap.put(NamedTextColor.DARK_AQUA, ChatColor.DARK_AQUA);
        namedToChatMap.put(NamedTextColor.DARK_RED, ChatColor.DARK_RED);
        namedToChatMap.put(NamedTextColor.DARK_PURPLE, ChatColor.DARK_PURPLE);
        namedToChatMap.put(NamedTextColor.GOLD, ChatColor.GOLD);
        namedToChatMap.put(NamedTextColor.GRAY, ChatColor.GRAY);
        namedToChatMap.put(NamedTextColor.DARK_GRAY, ChatColor.DARK_GRAY);
        namedToChatMap.put(NamedTextColor.BLUE, ChatColor.BLUE);
        namedToChatMap.put(NamedTextColor.GREEN, ChatColor.GREEN);
        namedToChatMap.put(NamedTextColor.AQUA, ChatColor.AQUA);
        namedToChatMap.put(NamedTextColor.RED, ChatColor.RED);
        namedToChatMap.put(NamedTextColor.LIGHT_PURPLE, ChatColor.LIGHT_PURPLE);
        namedToChatMap.put(NamedTextColor.YELLOW, ChatColor.YELLOW);
        namedToChatMap.put(NamedTextColor.WHITE, ChatColor.WHITE);
    }

    @Nullable
    public static TextColor fromChatColor(ChatColor rawColor)
    {
        if (rawColor == null) return null;

        var entry = namedToChatMap.entrySet().stream()
                .filter(e -> e.getValue() == rawColor).findFirst().orElse(null);

        return entry == null ? null : entry.getKey();
    }

    @Nullable
    public static ChatColor toChatColor(@Nullable TextColor rawColor)
    {
        if (rawColor == null) return null;

        var sourceColor = NamedTextColor.nearestTo(rawColor);

        return namedToChatMap.getOrDefault(sourceColor, ChatColor.WHITE);
    }
}
