package xiamomc.morph.abilities.impl;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.MorphAbility;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.messages.CommonStrings;
import xiamomc.morph.misc.MorphChatRenderer;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.messages.FormattableMessage;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ChatOverrideAbility extends MorphAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.CHAT_OVERRIDE;
    }

    @Initializer
    private void load(MorphConfigManager config)
    {
        config.onConfigRefresh(c ->
        {
            useCustomRenderer = config.getOrDefault(Boolean.class, ConfigOption.CHAT_OVERRIDE_USE_CUSTOM_RENDERER, true);
            patterns = config.getOrDefault(List.class, ConfigOption.CHAT_OVERRIDE_PATTERNS);
        }, true);
    }

    private boolean useCustomRenderer;

    private List<String> patterns = List.of();

    @Resolved
    private MorphManager morphs;

    @EventHandler
    public void onChat(AsyncChatEvent e)
    {
        var player = e.getPlayer();

        if (!appliedPlayers.contains(player)) return;

        var state = morphs.getDisguiseStateFor(player);
        assert state != null;

        var formattable = getFormattableMessage(state.getDisguiseIdentifier());

        if (useCustomRenderer)
            e.renderer(new MorphChatRenderer(formattable));
        else
        {
            //noinspection OverrideOnly
            e.renderer().render(player, state.getDisplayName(), e.message(), player);
        }
    }

    private FormattableMessage getFormattableMessage(String disguiseIdentifier)
    {
        var targetString = new AtomicReference<>(CommonStrings.chatOverrideString());

        for (var s : patterns)
        {
            var str = s.split(":", 3);

            if (str.length != 3)
                continue;

            var combined = str[0] + ":" + str[1];

            if (combined.equals(disguiseIdentifier))
            {
                targetString.set(new FormattableMessage(plugin, str[2]));
                break;
            }
        }

        return targetString.get();
    }
}
