package xyz.nifeather.morph.misc.integrations.placeholderapi.builtin;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.api.FeatherMorphAPI;
import xyz.nifeather.morph.misc.DisguiseTypes;
import xyz.nifeather.morph.misc.integrations.placeholderapi.IPlaceholderProvider;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class RedirectPlaceholderProvider extends MorphPluginObject implements IPlaceholderProvider
{
    /**
     * 获取此Placeholder提供器的ID
     *
     * @return 提供器ID
     */
    @Override
    public @NotNull String getPlaceholderIdentifier()
    {
        return "redirect";
    }

    protected @Nullable String stringIfNotDisguised(OfflinePlayer player, String placeholder)
    {
        return defaultString(player, placeholder);
    }

    protected @Nullable String defaultString(OfflinePlayer player, String placeholder)
    {
        return PlaceholderAPI.setPlaceholders(player, placeholder);
    }

    /**
     * 解析Placeholder
     *
     * @param player 玩家
     * @param params 参数
     * @return 内容，为null则会自动返回"???"
     */
    @Override
    public @Nullable String resolvePlaceholder(OfflinePlayer player, String params)
    {
        var placeholder = "%%%s%%".formatted(params.replaceFirst(getPlaceholderIdentifier(), ""));
        var api = Objects.requireNonNull(FeatherMorphAPI.instance());

        // Not disguised, return player itself's result
        var state = api.directAccess().morphManager().getDisguiseStateFor(player.getUniqueId());
        if (state == null)
            return stringIfNotDisguised(player, placeholder);

        // Not disguising as player, return player itself's result
        var type = state.getDisguiseType();
        if (type != DisguiseTypes.PLAYER)
            return defaultString(player, placeholder);

        var disguiseTarget = type.toStrippedId(state.getDisguiseIdentifier());

        // Disguising as a player that the server don't know, return empty
        var offlinePlayerIfExists = Bukkit.getOfflinePlayerIfCached(disguiseTarget);
        if (offlinePlayerIfExists == null)
            return defaultString(player, placeholder);

        return PlaceholderAPI.setPlaceholders(offlinePlayerIfExists, placeholder);
    }
}
