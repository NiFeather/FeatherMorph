package xyz.nifeather.morph.misc.integrations.placeholderapi.builtin;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
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
    private MorphManager morphManager;

    @Override
    public @Nullable String resolvePlaceholder(Player player, String param)
    {
        var builder = new StringBuilder();

        var data = morphManager.getDataStore().getIfLoaded(player.getUniqueId());
        if (data == null)
            return "Data not loaded";

        var list = data.getUnlockedDisguises();

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
