package xyz.nifeather.morph.messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.utilities.PluginAssetUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class OverlayingMessageStore
{
    private final String targetLocale;
    public String targetLocale()
    {
        return targetLocale;
    }

    protected final Logger logger;
    protected final FeatherMorphMain plugin;
    protected final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    // Message Key <-> Value
    protected final Map<String, String> i18nMap = new ConcurrentHashMap<>();

    public OverlayingMessageStore(String targetLocale)
    {
        this.targetLocale = targetLocale;
        plugin = FeatherMorphMain.getInstance();
        logger = FeatherMorphMain.getInstance().getSLF4JLogger();
    }

    public Optional<String> lookup(String messageKey)
    {
        return Optional.ofNullable(i18nMap.getOrDefault(messageKey, null));
    }

    public void load()
    {
        loadFromPluginAsset();

        var messagesDirectory = new File(plugin.getDataFolder(), "messages");
        var overrideFile = new File(messagesDirectory, "%s.json".formatted(targetLocale));
        if (overrideFile.exists())
        {
            this.loadFromFileSystem(overrideFile);
        }
        else if (FeatherMorphMain.getInstance().debugOutputEnabled())
        {
            logger.info("Override file '%s' not exist".formatted(overrideFile.getAbsolutePath()));
        }
    }

    private void loadFromFileSystem(File i18nFile)
    {
        Map<String, String> fileI18nMap;
        try
        {
            var str = Files.readString(i18nFile.toPath());
            fileI18nMap = gson.fromJson(str, new TypeToken<>(){});

            this.i18nMap.putAll(fileI18nMap);
        }
        catch (Exception e)
        {
            logger.error("Unable to read i18n for language %s from filesystem".formatted(targetLocale), e);
        }
    }

    public void clear()
    {
        this.i18nMap.clear();
    }

    public void reload()
    {
        clear();
        load();
    }

    public boolean isEmpty()
    {
        return this.i18nMap.isEmpty();
    }

    private void loadFromPluginAsset()
    {
        var path = PluginAssetUtils.langPath(targetLocale);
        var asset = PluginAssetUtils.getFileStringsOptional(path);
        if (asset.isEmpty())
        {
            logger.info("Skipping %s from plugin assets because it doesn't exists".formatted(targetLocale));
            return;
        }

        var typeToken = new TypeToken<Map<String, String>>(){}.getType();

        try
        {
            Map<String, String> assetI18n = gson.fromJson(asset.get(), typeToken);
            this.i18nMap.putAll(assetI18n);
        }
        catch (Exception e)
        {
            logger.error("Unable to read i18n for language %s from plugin assets".formatted(targetLocale), e);
        }
    }
}
