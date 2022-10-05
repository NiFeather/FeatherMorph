package xiamomc.morph.misc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;

public class MessageUtils
{
    public static Component prefixes(CommandSender sender, Component[] c)
    {
        if (!(sender instanceof Player))
            return Component.translatable("%s", c);

        var list = new ArrayList<Component>();
        list.add(Component.text("\uE30D"));
        list.addAll(Arrays.stream(c).toList());

        return Component.translatable("text.hub.hint", list).color(TextColor.fromCSSHexString("#f2f2f2"));
    }

    public static Component prefixes(CommandSender sender, Component c)
    {
        return prefixes(sender, new Component[]{c});
    }
}
