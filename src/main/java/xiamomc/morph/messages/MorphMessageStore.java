package xiamomc.morph.messages;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Messages.IStrings;
import xiamomc.pluginbase.Messages.MessageStore;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MorphMessageStore extends MessageStore<MorphPlugin>
{
    private final List<Class<? extends IStrings>> strings = ObjectList.of(
            CommonStrings.class,
            CommandStrings.class,
            HelpStrings.class,
            MorphStrings.class,
            RequestStrings.class,
            SkillStrings.class,
            CommandNameStrings.class,
            HintStrings.class
    );

    private final Map<String, MorphMessageSubStore> subStores = new Object2ObjectOpenHashMap<>();

    private final Bindable<String> serverLanguage = new Bindable<>();

    @Initializer
    private void load(MorphConfigManager config)
    {
        config.bind(serverLanguage, ConfigOption.LANGUAGE_CODE);
    }

    private MorphMessageSubStore getOrCreateSubStore(String locale)
    {
        MorphMessageSubStore store;

        synchronized (subStores)
        {
            store = subStores.getOrDefault(locale, null);

            if (store != null) return store;

            store = new MorphMessageSubStore(locale, strings, this);
            store.initializeStorage();
            store.saveConfiguration();

            subStores.put(locale, store);
        }

        return store;
    }

    public Map<String, String> getAllMessages()
    {
        return storingObject;
    }

    @Override
    protected @NotNull String getFileName()
    {
        return "messages/default.json";
    }

    @Override
    public String get(String key, @Nullable String defaultValue, @Nullable String locale)
    {
        var serverLanguage = this.serverLanguage.get();

        if (locale == null || locale.isBlank() || locale.isEmpty())
        {
            logger.warn("Resolving message key " + key + " for null or empty locale");
            locale = serverLanguage;
        }

        var messageStores = ObjectArrayList.of(
                this.getOrCreateSubStore("override"),
                this.getOrCreateSubStore(locale)
        );

        if (!locale.equals(serverLanguage))
            messageStores.add(this.getOrCreateSubStore(serverLanguage));

        for (var store : messageStores)
        {
            var msg = store.get(key, null, null);
            if (msg != null) return msg;
        }

        return super.get(key, defaultValue == null ? "%s@%s".formatted(key, locale) : defaultValue, null);
    }

    @Override
    public boolean reloadConfiguration()
    {
        var allSuccess = new AtomicBoolean(true);

        subStores.forEach((l, s) ->
        {
            if (!s.reloadConfiguration()) allSuccess.set(false);
        });

        return super.reloadConfiguration() && allSuccess.get();
    }

    @Override
    protected List<Class<? extends IStrings>> getStrings()
    {
        return strings;
    }

    @Override
    protected String getPluginNamespace()
    {
        return MorphPlugin.getMorphNameSpace();
    }
}
