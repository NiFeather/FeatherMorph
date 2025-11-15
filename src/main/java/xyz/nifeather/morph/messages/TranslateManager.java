package xyz.nifeather.morph.messages;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xiamomc.pluginbase.Messages.IStrings;
import xiamomc.pluginbase.Messages.MessageStore;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.misc.ExecutionErrorException;
import xyz.nifeather.morph.utilities.PluginAssetUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TranslateManager
{
    private static final TranslateManager instance = new TranslateManager();
    private final Logger logger = LoggerFactory.getLogger("FeatherMorph$TranslateManager");

    public static TranslateManager instance()
    {
        return instance;
    }

    // Locale <-> Store
    private final Map<String, OverlayingMessageStore> messageStoreMap = new ConcurrentHashMap<>();

    public TranslateManager()
    {
        init();
    }

    public static final String FALLBACK_LOCALE = "en_us";

    private void init()
    {
        logger.info("Initializing translate manager");

        setupLanguage(FALLBACK_LOCALE);

        var messagesDirectory = new File(FeatherMorphMain.getInstance().getDataFolder(), "messages");
        for (String lang : PluginAssetUtils.allSupportedLanguages())
        {
            try
            {
                logger.info("Extracting default locale files... (This won't affect existing files)");
                PluginAssetUtils.extractLocaleFile(lang, messagesDirectory, false);
                logger.info("Done Extracting default locale files");
            }
            catch (ExecutionErrorException e)
            {
                logger.warn("Error occurred extracting default locale files, ignoring", e);
            }
        }

        logger.info("Done initializing translate manager");
    }

    public void setupLanguage(String locale)
    {
        var store = new OverlayingMessageStore(locale);

        try
        {
            store.load();
        }
        catch (Throwable t)
        {
            logger.error("Failed to load i18n store for language %s".formatted(locale), t);
        }

        messageStoreMap.put(locale, store);
        logger.info("Done loading i18n store for language %s".formatted(locale));
    }

    public OverlayingMessageStore lookTranslateStoreOrCreate(String locale)
    {
        if (!messageStoreMap.containsKey(locale))
            setupLanguage(locale);

        return lookupTranslateStore(locale).orElseThrow();
    }

    public Optional<OverlayingMessageStore> lookupTranslateStore(String locale)
    {
        return Optional.ofNullable(messageStoreMap.getOrDefault(locale, null));
    }

    public OverlayingMessageStore getDefaultTranslateStore()
    {
        return lookupTranslateStore(FALLBACK_LOCALE).orElseThrow(() -> new NullDependencyException("No"));
    }


    public Optional<String> lookupTranslate(String rawLocale, String messageKey)
    {
        return lookupTranslate(rawLocale, messageKey, true);
    }

    public Optional<String> lookupTranslate(String rawLocale, String messageKey, boolean allowFallback)
    {
        var locale = rawLocale.toLowerCase(Locale.ROOT);

        var translateStore = lookTranslateStoreOrCreate(rawLocale);

        var result = translateStore.lookup(messageKey);
        if (result.isPresent()) return result; // 如果存在对应的语言文件，则返回

        // 不然，如果允许我们检查fallback，则再去检查 FALLBACK_LOCALE 里对应的存储
        if (allowFallback && !translateStore.targetLocale().equals(FALLBACK_LOCALE))
        {
            var defaultResult = lookupTranslate(FALLBACK_LOCALE, messageKey);
            return defaultResult.or(() -> Optional.of("[%s:%s]".formatted(locale, messageKey)));
        }

        return result;
    }

    public void reload()
    {
        messageStoreMap.values().forEach(OverlayingMessageStore::reload);
    }

    public MessageStore<FeatherMorphMain> asFrameworkMessageStore()
    {
        return new FrameworkMessageStoreBinding(this);
    }

    public static class FrameworkMessageStoreBinding extends MessageStore<FeatherMorphMain>
    {
        private final TranslateManager translate;

        public FrameworkMessageStoreBinding(TranslateManager translateManager)
        {
            this.translate = translateManager;
            storingObject = new HashMap<>();
        }

        @Override
        protected List<Class<? extends IStrings>> getStrings()
        {
            return List.of();
        }

        @Override
        protected String getPluginNamespace()
        {
            return FeatherMorphMain.getMorphNameSpace();
        }

        @Override
        public void initializeStorage(boolean noReload)
        {
        }

        @Override
        public void addMissingStrings()
        {
        }

        @Override
        public String get(String key, @Nullable String defaultValue, @Nullable String locale)
        {
            return translate.lookupTranslate("override", key, false)
                    .orElseGet(() -> translate.lookupTranslate(locale == null ? FALLBACK_LOCALE : locale, key).orElse(key));
        }
    }
}
