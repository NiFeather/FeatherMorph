package xyz.nifeather.morph.misc.integrations.placeholderapi.builtin;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.interfaces.IManagePlayerData;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.misc.integrations.placeholderapi.IPlaceholderProvider;

public class AvaliableDisguisesProvider extends MorphPluginObject implements IPlaceholderProvider
{
    @Override
    public @NotNull String getPlaceholderIdentifier()
    {
        return "available";
    }

    @Resolved
    private IManagePlayerData data;

    @Override
    public @Nullable String resolvePlaceholder(OfflinePlayer offlinePlayer, String param)
    {
        var player = offlinePlayer.getPlayer();
        if (player == null)
            return "err_not_online";

        var builder = new StringBuilder();
        var list = data.getAvailableDisguisesFor(player);

        var locale = MessageUtils.getServerLocale();

        switch (param)
        {
            case "name" ->
            {
                list.forEach(i ->
                {
                    builder.append(PlainTextComponentSerializer.plainText().serialize(i.asComponent(locale)));

                    if (list.iterator().hasNext())
                        builder.append(", ");
                });

                return builder.toString();
            }

            case "count" ->
            {
                return String.valueOf(list.size());
            }
        }

        return null;
    }
}
