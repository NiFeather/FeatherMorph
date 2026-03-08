package xyz.nifeather.morph.messages.vanilla;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;
import xyz.nifeather.morph.config.ConfigOptions;
import xyz.nifeather.morph.config.MorphConfigManager;

import java.util.Locale;
import java.util.Map;

/**
 * Minecraft语言相关
 */
public class MasterVanillaMessageStore extends BasicVanillaMessageStore //todo: MasterVanillaMessageStore should not extends BasicVanillaMessageStore
{
    @Initializer
    private void load(MorphConfigManager config)
    {
        config.bind(serverLocale, ConfigOptions.LANGUAGE_CODE);

        serverLocale.onValueChanged((o, n) ->
        {
            if (n.equalsIgnoreCase("en_us")) return;

            this.getOrCreateSubStore(n);
        }, true);
    }

    private final Bindable<String> serverLocale = new Bindable<>("en_us");

    private final Map<String, VanillaMessageSubStore> subStores = new Object2ObjectOpenHashMap<>();

    @Nullable
    public synchronized BasicVanillaMessageStore getOrCreateSubStore(String locale)
    {
        locale = locale.toLowerCase(Locale.ROOT);

        if (locale.equalsIgnoreCase("en_us"))
            return this; //todo: return a real en_us substore, instead of the MasterVanillaMessageStore

        VanillaMessageSubStore store = null;

        synchronized (subStores)
        {
            store = subStores.getOrDefault(locale, null);

            if (store != null) return store;

            store = new VanillaMessageSubStore(locale);
            store.initializeStorage();
            store.saveConfiguration();

            subStores.put(locale, store);
        }

        return store;
    }

    public Component getComponent(String key, @Nullable String defaultValue, @Nullable String locale)
    {
        var msg = get(key, defaultValue, locale);
        return msg == null ? Component.translatable(key) : Component.text(msg);
    }

    @Override
    public String get(String key, @Nullable String defaultValue, @Nullable String locale)
    {
        if (locale == null || locale.isBlank())
        {
            logger.warn("Resolving key " + key + " for null or empty locale");
            locale = "en_us";
        }

        locale = locale.toLowerCase(Locale.ROOT);

        if (locale.equals("en_us"))
            return super.get(key, defaultValue, null);

        var store = this.getOrCreateSubStore(locale);

        if (store == null || store.equals(this))
            return super.get(key, defaultValue, null);
        else
            return store.get(key, defaultValue, locale);
    }

    @Override
    public boolean reloadConfiguration()
    {
        this.subStores.clear();

        return super.reloadConfiguration();
    }

    @Override
    protected @NotNull String getLocaleCode()
    {
        return serverLocale.get();
    }
}
