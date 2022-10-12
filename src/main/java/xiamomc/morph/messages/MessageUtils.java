package xiamomc.morph.messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.messages.FormattableMessage;

public class MessageUtils extends MorphPluginObject
{
    @Resolved(shouldSolveImmediately = true)
    private static MorphConfigManager configManager;

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static Component prefixes(CommandSender sender, Component[] c)
    {
        if (!(sender instanceof Player))
            return Component.translatable("%s", c);

        var finalComponent = Component.empty();

        for (var cc : c)
            finalComponent = finalComponent.append(cc);

        return CommonStrings.pluginMessageString()
                .resolve("message", finalComponent)
                .toComponent();
    }

    public static Component prefixes(CommandSender sender, Component c)
    {
        return prefixes(sender, new Component[]{c});
    }

    public static Component prefixes(CommandSender sender, FormattableMessage formattable)
    {
        return prefixes(sender, formattable.toComponent());
    }
}
