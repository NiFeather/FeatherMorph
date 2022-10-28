package xiamomc.morph.abilities.impl;

import io.papermc.paper.event.player.AsyncChatEvent;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.IMorphAbility;
import xiamomc.morph.abilities.MorphAbility;
import xiamomc.morph.abilities.options.ChatOverrideOption;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.messages.CommonStrings;
import xiamomc.morph.misc.MorphChatRenderer;
import xiamomc.morph.storage.skill.ISkillOption;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.messages.FormattableMessage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ChatOverrideAbility extends MorphAbility<ChatOverrideOption>
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.CHAT_OVERRIDE;
    }

    private final ChatOverrideOption option = new ChatOverrideOption();

    @Override
    public ISkillOption getOption()
    {
        return option;
    }

    private final Map<String, ChatOverrideOption> optionMap = new Object2ObjectOpenHashMap<>();

    @Override
    public boolean setOption(@NotNull String id, @Nullable ChatOverrideOption option)
    {
        optionMap.put(id, option);

        return true;
    }

    @Override
    public void clearOptions()
    {
        optionMap.clear();
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
        var formattable = getFormattableMessage(state.getDisguiseIdentifier());

        if (formattable == null)
            formattable = getFormattableMessage(state.getSkillIdentifier());

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

        var p = optionMap.get(identifier);

        if (p != null)
            targetString.set(new FormattableMessage(plugin, p.getMessagePattern()));

        return targetString.get();
    }
}
