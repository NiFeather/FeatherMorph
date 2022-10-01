package xiamomc.morph.misc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.ArrayList;
import java.util.Arrays;

public class MessageUtils
{
    public static Component prefixes(Component[] c)
    {
        var list = new ArrayList<Component>();
        list.add(Component.text("\uE30D"));
        list.addAll(Arrays.stream(c).toList());

        return Component.translatable("text.hub.hint", list).color(TextColor.fromCSSHexString("#dddddd"));
    }

    public static Component prefixes(Component c)
    {
        return prefixes(new Component[]{c});
    }
}
