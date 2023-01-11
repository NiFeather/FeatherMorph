package xiamomc.morph.messages;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.pluginbase.Managers.DependencyManager;
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
                .withLocale(getLocale(sender))
                .resolve("message", finalComponent)
                .toComponent(null);
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
        if (formattable.getLocale() == null)
            formattable.withLocale(getLocale(sender));

        return prefixes(sender, formattable.toComponent(null));
    }

    @NotNull
    public static String getLocale(Player player)
    {
        return player.locale().toLanguageTag().replace('-', '_').toLowerCase();
    }

    @NotNull
    public static String getLocaleOr(CommandSender sender, @NotNull String defaultValue)
    {
        var locale = getLocale(sender);
        return locale == null ? defaultValue : locale;
    }

    private static MorphConfigManager configManager;

    private static MorphConfigManager getConfigManager()
    {
        if (configManager != null)
            return configManager;

        var depMgr = DependencyManager.getInstance(MorphPlugin.getMorphNameSpace());
        var config = depMgr.get(MorphConfigManager.class);

        configManager = config;

        return config;
    }

    @Nullable
    private static String getServerLocale()
    {
        var config = getConfigManager();

        if (config == null) return null;
        else return config.getOrDefault(String.class, ConfigOption.LANGUAGE_CODE);
    }

    @Nullable
    public static String getLocale(CommandSender sender)
    {
        if (sender instanceof Player player)
            return getLocale(player);
        else
            return getServerLocale();
    }
}
