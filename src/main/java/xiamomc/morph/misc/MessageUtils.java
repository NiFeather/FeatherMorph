package xiamomc.morph.misc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.pluginbase.Annotations.Resolved;

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

        return miniMessage.deserialize(configManager.getOrDefault(String.class, ConfigOption.MESSAGE_PATTERN),
                Placeholder.component("message", finalComponent));
    }

    public static Component prefixes(CommandSender sender, Component c)
    {
        return prefixes(sender, new Component[]{c});
    }
}
