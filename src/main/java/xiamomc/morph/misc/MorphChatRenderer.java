package xiamomc.morph.misc;

import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.messages.CommonStrings;
import xiamomc.pluginbase.Annotations.Resolved;

public class MorphChatRenderer extends MorphPluginObject implements ChatRenderer
{
    private Component message;

    @Resolved(shouldSolveImmediately = true)
    private MorphManager morphManager;

    @Override
    public @NotNull Component render(@NotNull Player source, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer)
    {
        if (this.message == null)
        {
            var state = morphManager.getDisguiseStateFor(source);
            if (state != null && state.getDisguise().isPlayerDisguise() && source.hasPermission("xiamomc.morph.chatoverride"))
            {
                sourceDisplayName = state.getDisplayName();
                var plainText = PlainTextComponentSerializer.plainText().serialize(message);

                if (plainText.length() > 20) plainText = plainText.substring(0, 20) + "...";

                Logger.info("正在覆盖" + source.getName() + "的消息：" + plainText);
            }

            try
            {
                this.message = CommonStrings.chatOverrideString()
                        .resolve("who", sourceDisplayName)
                        .resolve("message", message).toComponent();
            }
            catch (Throwable t)
            {
                Logger.warn("格式化消息时出现错误：" + t.getMessage());
                t.printStackTrace();
            }
        }

        return this.message;
    }
}
