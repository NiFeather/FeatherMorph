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
        var placeholder = "%%%s%%".formatted(params.replaceFirst("redirect", ""));
        var api = Objects.requireNonNull(FeatherMorphAPI.instance());

        // Not disguised, return player itself's result
        var state = api.directAccess().morphManager().getDisguiseStateFor(player.getUniqueId());
        if (state == null)
            return PlaceholderAPI.setPlaceholders(player, placeholder);

        // Not disguising as player, return player itself's result
        var type = state.getDisguiseType();
        if (type != DisguiseTypes.PLAYER)
            return PlaceholderAPI.setPlaceholders(player, placeholder);

        var disguiseTarget = type.toStrippedId(state.getDisguiseIdentifier());

        // Disguising as a player that the server don't know, return empty
        var offlinePlayerIfExists = Bukkit.getOfflinePlayerIfCached(disguiseTarget);
        if (offlinePlayerIfExists == null)
            return PlaceholderAPI.setPlaceholders(player, placeholder);

        return PlaceholderAPI.setPlaceholders(offlinePlayerIfExists, placeholder);
    }
}
