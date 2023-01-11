package xiamomc.morph.misc;

import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.messages.CommonStrings;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Messages.FormattableMessage;

public class MorphChatRenderer extends MorphPluginObject implements ChatRenderer
{
    private Component message;

    private Component messageRevealed;

    @Resolved(shouldSolveImmediately = true)
    private MorphManager morphManager;

    public MorphChatRenderer(FormattableMessage formattable)
    {
        if (formattable != null)
            this.formattable = formattable;
    }

    private FormattableMessage formattable = CommonStrings.chatOverrideString();

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName,
                                     @NotNull Component incomingMessage, @NotNull Audience viewer)
    {
        if (this.message == null)
        {
            var state = morphManager.getDisguiseStateFor(source);

            if (state != null)
                sourceDisplayName = state.getPlayerDisplayName();

            try
            {
                this.message = buildMessage(sourceDisplayName, incomingMessage, source);

                this.messageRevealed = buildMessage(sourceDisplayName.append(Component.text("(" + source.getName() + ")")), incomingMessage, source);
            }
            catch (Throwable t)
            {
                logger.error("格式化消息时出现错误：" + t.getMessage());
                t.printStackTrace();
            }
        }

        return viewer instanceof Permissible permissible
                ? permissible.hasPermission("xiamomc.morph.chatoverride.reveal")
                    ? this.messageRevealed
                    : this.message
                : this.message;
    }

    private Component buildMessage(Component displayName, Component msg, Player player)
    {
        var locale = MessageUtils.getLocale(player);

        return new FormattableMessage(MorphPlugin.getMorphNameSpace(), formattable.getKey(), formattable.getDefaultString())
                .withLocale(locale)
                .resolve("who", displayName)
                .resolve("message", msg)
                .toComponent(null);
    }
}
