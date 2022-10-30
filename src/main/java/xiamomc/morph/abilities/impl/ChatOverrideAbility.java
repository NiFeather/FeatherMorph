package xiamomc.morph.abilities.impl;

import com.destroystokyo.paper.ClientOption;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.MorphAbility;
import xiamomc.morph.abilities.options.ChatOverrideOption;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.misc.MorphChatRenderer;
import xiamomc.morph.storage.skill.ISkillOption;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.messages.FormattableMessage;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class ChatOverrideAbility extends MorphAbility<ChatOverrideOption>
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.CHAT_OVERRIDE;
    }

    @Override
    protected ISkillOption createOption()
    {
        return new ChatOverrideOption();
    }

    @Initializer
    private void load(MorphConfigManager config)
    {
        config.onConfigRefresh(c ->
        {
            useCustomRenderer = config.getOrDefault(Boolean.class, ConfigOption.CHAT_OVERRIDE_USE_CUSTOM_RENDERER, true);
        }, true);
    }

    private boolean useCustomRenderer;

    @Resolved
    private MorphManager morphs;

    @EventHandler
    public void onChat(AsyncChatEvent e)
    {
        var player = e.getPlayer();

        if (!appliedPlayers.contains(player)) return;

        var state = morphs.getDisguiseStateFor(player);
        assert state != null;

        //先从id获取，再fallback到技能ID
        var formattable = getOr(
                getFormattableMessage(state.getDisguiseIdentifier()),
                Objects::nonNull,
                getFormattableMessage(state.getSkillIdentifier()));

        if (useCustomRenderer)
            e.renderer(new MorphChatRenderer(formattable));
        else
        {
            //noinspection OverrideOnly
            e.renderer().render(player, state.getDisplayName(), e.message(), player);
        }
    }

    @Nullable
    private FormattableMessage getFormattableMessage(String identifier)
    {
        var targetString = new AtomicReference<FormattableMessage>();

        var p = options.get(identifier);

        if (p != null)
            targetString.set(new FormattableMessage(plugin, p.getMessagePattern()));

        return targetString.get();
    }
}
