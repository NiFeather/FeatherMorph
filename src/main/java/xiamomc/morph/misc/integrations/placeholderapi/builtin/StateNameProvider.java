package xiamomc.morph.misc.integrations.placeholderapi.builtin;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.integrations.placeholderapi.IPlaceholderProvider;
import xiamomc.morph.misc.integrations.placeholderapi.MatchMode;
import xiamomc.pluginbase.Annotations.Resolved;

public class StateNameProvider extends MorphPluginObject implements IPlaceholderProvider
{
    @Override
    public @NotNull String getPlaceholderIdentifier()
    {
        return "state";
    }

    @Resolved
    private MorphManager morphs;

    @Override
    public @Nullable String resolvePlaceholder(Player player, String params)
    {
        var type = params.replace(getPlaceholderIdentifier(), "");
        var state = morphs.getDisguiseStateFor(player);

        if (type.equalsIgnoreCase("_id"))
        {
            return state != null
                    ? state.getDisguiseIdentifier()
                    : "无";
        }
        else if (type.equalsIgnoreCase("_name"))
        {
            return state != null
                    ? PlainTextComponentSerializer.plainText().serialize(state.getServerDisplay())
                    : "无";
        }

        return null;
    }

    @Override
    public MatchMode getMatchMode()
    {
        return MatchMode.Prefixes;
    }
}
