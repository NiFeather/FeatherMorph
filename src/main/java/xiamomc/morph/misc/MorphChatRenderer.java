package xiamomc.morph.misc;

import io.papermc.paper.chat.ChatRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
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
            if (state != null && state.getDisguise().isPlayerDisguise())
                sourceDisplayName = state.getDisplayName();

            this.message = Component.text("≡ ")
                    .append(sourceDisplayName)
                    .append(Component.text(" » "))
                    .append(message)
                    .color(TextColor.fromHexString("#dddddd"));
        }

        return this.message;
    }
}
