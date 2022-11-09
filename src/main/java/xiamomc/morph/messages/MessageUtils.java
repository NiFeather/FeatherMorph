package xiamomc.morph.messages;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xiamomc.morph.MorphPluginObject;
import xiamomc.pluginbase.Messages.FormattableMessage;

public class MessageUtils extends MorphPluginObject
{
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

    public static Component prefixes(CommandSender sender, String str)
    {
        return prefixes(sender, Component.text(str));
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
