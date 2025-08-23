package xyz.nifeather.morph.abilities.impl;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xiamomc.pluginbase.Messages.MessageStore;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.abilities.MorphAbility;
import xyz.nifeather.morph.abilities.options.ChatOverrideOption;
import xyz.nifeather.morph.api.morphs.abilities.AbilityNames;
import xyz.nifeather.morph.config.ConfigOption;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.MorphChatRenderer;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

import java.util.concurrent.atomic.AtomicReference;

public class ChatOverrideAbility extends MorphAbility<ChatOverrideOption>
{
    @Override
    public @NotNull ISkillAbilityOptionHandler<ChatOverrideOption> optionHandler()
    {
        return ChatOverrideOption.OPTION_HANDLER;
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityNames.CHAT_OVERRIDE;
    }

    @Initializer
    private void load(MorphConfigManager config, MessageStore<?> messageStore)
    {
        config.bind(allowChatOverride, ConfigOption.ALLOW_CHAT_OVERRIDE);
        config.bind(useCustomRenderer, ConfigOption.CHAT_OVERRIDE_USE_CUSTOM_RENDERER);
    }

    private final Bindable<Boolean> useCustomRenderer = new Bindable<>(false);
    private final Bindable<Boolean> allowChatOverride = new Bindable<>(true);

    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        return true;
    }

    @Resolved
    private MorphManager morphs;

    @EventHandler
    public void onChat(AsyncChatEvent e)
    {
        if (!allowChatOverride.get()) return;

        var player = e.getPlayer();

        if (!isPlayerApplied(player) || !player.hasPermission(CommonPermissions.CHAT_OVERRIDE)) return;

        var state = morphs.getDisguiseStateFor(player);
        assert state != null;

        //先从id获取，再fallback到技能ID
        var formattable = this.getFormattableMessage(state);

        if (useCustomRenderer.get())
            e.renderer(new MorphChatRenderer(formattable));
        else
        {
            //noinspection OverrideOnly
            e.renderer().render(player, state.getServerDisplay(), e.message(), player);
        }
    }

    @Nullable
    private FormattableMessage getFormattableMessage(DisguiseState state)
    {
        var targetString = new AtomicReference<FormattableMessage>();

        var p = this.getOptionFor(state);

        if (p != null)
        {
            var pattern = p.getMessagePattern();

            if (pattern == null) return null;

            targetString.set(new FormattableMessage(plugin, pattern));
        }

        return targetString.get();
    }
}
