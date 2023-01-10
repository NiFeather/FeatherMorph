package xiamomc.morph.messages.vanilla;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.messages.MorphMessageSubStore;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Messages.IStrings;
import xiamomc.pluginbase.Messages.MessageStore;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Minecraft语言相关
 */
public class VanillaMessageStore extends BasicVanillaMessageStore
{
    @Initializer
    private void load(MorphConfigManager config)
    {
        config.bind(localeCode, ConfigOption.LANGUAGE_CODE);
        config.bind(allowTranslatable, ConfigOption.LANGUAGE_ALLOW_FALLBACK);

        localeCode.onValueChanged((o, n) ->
        {
            if (n.equalsIgnoreCase("en_us")) return;

            this.getOrCreateSubStore(n);
        }, true);
    }

    private static Bindable<Boolean> allowTranslatable = new Bindable<>(false);

    private Bindable<String> localeCode = new Bindable<>("en_us");

    private final Map<String, VanillaMessageSubStore> subStores = new Object2ObjectOpenHashMap<>();

    public synchronized BasicVanillaMessageStore getOrCreateSubStore(String locale)
    {
        if (locale.equalsIgnoreCase("en_us")) return this;

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
        if (locale == null)
            logger.warn("Resolving vanilla key " + key + " for null locale");

        if (locale == null || locale.isBlank() || locale.isEmpty() || locale.equals("en_us"))
            return super.get(key, defaultValue, null);

        var store = this.getOrCreateSubStore(locale);

        return store.get(key, defaultValue, locale);
    }

    @Override
    protected @NotNull String getLocaleCode()
    {
        return localeCode.get();
    }
}
