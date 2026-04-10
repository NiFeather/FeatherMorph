package xyz.nifeather.morph.misc.integrations.placeholderapi.builtin;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RedirectOrEmptyPlaceholderProvider extends RedirectPlaceholderProvider
{
    /**
     * 获取此Placeholder提供器的ID
     *
     * @return 提供器ID
     */
    @Override
    public @NotNull String getPlaceholderIdentifier()
    {
        return "orempty";
    }

    @Override
    protected @Nullable String stringIfNotDisguised(OfflinePlayer player, String placeholder)
    {
        return PlaceholderAPI.setPlaceholders(player, placeholder);
    }

    @Override
    protected @Nullable String defaultString(OfflinePlayer player, String placeholder)
    {
        return "";
    }
}
