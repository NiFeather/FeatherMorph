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
import xiamomc.pluginbase.Bindables.Bindable;
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
        if (isSingleLanguage())
            return getServerLocale();

        return player.locale().toLanguageTag().replace('-', '_').toLowerCase();
    }

    @NotNull
    public static String getLocaleOr(CommandSender sender, @NotNull String defaultValue)
    {
        var locale = getLocale(sender);
        return locale == null ? defaultValue : locale;
    }

    private static MorphConfigManager configManager;

    private static void initializeConfigManager()
    {
        if (configManager != null) return;

        var depMgr = DependencyManager.getInstance(MorphPlugin.getMorphNameSpace());
        var config = depMgr.get(MorphConfigManager.class);

        if (config != null)
        {
            config.bind(serverLocale, ConfigOption.LANGUAGE_CODE);
            config.bind(singleLanguage, ConfigOption.SINGLE_LANGUAGE);
        }

        configManager = config;
    }

    private final static Bindable<String> serverLocale = new Bindable<>("zh_cn");
    private final static Bindable<Boolean> singleLanguage = new Bindable<>(false);

    private static String getServerLocale()
    {
        initializeConfigManager();

        return serverLocale.get();
    }

    private static boolean isSingleLanguage()
    {
        initializeConfigManager();

        return singleLanguage.get();
    }

    @Nullable
    public static String getLocale(CommandSender sender)
    {
        if (sender instanceof Player player && !isSingleLanguage())
            return getLocale(player);
        else
            return getServerLocale();
    }
}
