package xiamomc.morph.misc.integrations.placeholderapi.builtin;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.interfaces.IManagePlayerData;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.misc.integrations.placeholderapi.IPlaceholderProvider;
import xiamomc.morph.misc.integrations.placeholderapi.MatchMode;
import xiamomc.pluginbase.Annotations.Resolved;

public class AvaliableDisguisesProvider extends MorphPluginObject implements IPlaceholderProvider
{
    @Override
    public @NotNull String getPlaceholderIdentifier()
    {
        return "avaliable";
    }

    @Resolved
    private IManagePlayerData data;

    @Override
    public @Nullable String resolvePlaceholder(Player player, String params)
    {
        var type = params.replace(getPlaceholderIdentifier(), "");
        var builder = new StringBuilder();
        var list = data.getAvaliableDisguisesFor(player);

        var locale = MessageUtils.getServerLocale();

        if (type.equalsIgnoreCase("_name"))
        {
            list.forEach(i ->
            {
                builder.append(PlainTextComponentSerializer.plainText().serialize(i.asComponent(locale)));

                if (list.iterator().hasNext())
                    builder.append(", ");
            });

            return builder.toString();
        }
        else if (type.equalsIgnoreCase("_count"))
        {
            return String.valueOf(list.size());
        }

        return null;
    }

    @Override
    public MatchMode getMatchMode()
    {
        return MatchMode.Prefixes;
    }
}
