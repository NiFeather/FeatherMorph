package xyz.nifeather.morph.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.MorphPluginObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DirectoryJsonBasedStorage<T> extends MorphPluginObject
{
    protected final DirectoryStorage directoryStorage;

    protected abstract T getDefault();

    protected Gson createGson()
    {
        return new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }

    protected final Gson gson;

    protected DirectoryJsonBasedStorage(String dirBaseName)
    {
        this.directoryStorage = new DirectoryStorage(dirBaseName);

        this.gson = createGson();

        if (this.directoryStorage.initializeFailed())
            logger.warn("Failed initializing directory storage, please see errors above.");
    }

    private final Map<String, Object> instancesMap = new ConcurrentHashMap<>();

    public void clearCache()
    {
        this.instancesMap.clear();
    }

    private final AtomicInteger packageVersion = new AtomicInteger(-3);

    public void setPackageVersion(int version)
    {
        if (version == -3)
            throw new IllegalArgumentException("Cannot set package version to -3");

        var file = directoryStorage.getFile("package_version.txt", true);
        this.packageVersion.set(version);

        if (file == null)
        {
            logger.warn("Can't write package version to file, it will not be saved across sessions.");
            return;
        }

        try
        {
            FileUtils.writeStringToFile(file, "" + version, StandardCharsets.UTF_8);
        }
        catch (Throwable t)
        {
            logger.warn("Can't write package version to file, it will not be saved across sessions: " + t.getMessage());
        }
    }

    public int getPackageVersion()
    {
        if (packageVersion.get() != -3)
            return packageVersion.get();

        var file = directoryStorage.getFile("package_version.txt", false);
        if (file == null) return -1;

        try
        {
            String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

            int version = Integer.parseInt(content);
            packageVersion.set(version);
            return version;
        }
        catch (Throwable t)
        {
            logger.error("Can't get package version: " + t.getMessage());
            t.printStackTrace();

            return -1;
        }
    }

    /**
     * Gets the path to the key
     * @apiNote You may need to manually add the file extension after calling this
     * @return NULL if the given identifier is illegal, for example, contains multiple ":"
     */
    @Nullable
    public String getPath(String key)
    {
        return key.replace(":", "/")
                .replace("@", "$")
                .replaceAll("[^a-zA-Z0-9\\-]]", "_");
    }

    /**
     * @param key The file name
     * @return NULL if the file does not exist or cannot be read, or there's an error during convert
     */
    @Nullable
    public T get(String key)
    {
        key = getPath(key);

        var cached = instancesMap.getOrDefault(key, null);
        if (cached != null) return cached == getDefault() ? null : (T) cached;

        var file = directoryStorage.getFile(key + ".json", false);
        if (file == null) return null;

        if (!file.canRead())
        {
            logger.warn("The file '%s' cannnot be read.".formatted(file.getPath()));
            return null;
        }

        Object obj;

        try (InputStreamReader fileStream = new InputStreamReader(new FileInputStream(file)))
        {
            obj = gson.fromJson(fileStream, getDefault().getClass());
        }
        catch (Throwable t)
        {
            logger.warn("Can't convert from JSON: " + t.getMessage());
            return null;
        }

        if (obj == null)
            obj = getDefault();

        this.instancesMap.put(key, obj);
        return obj == getDefault() ? null : (T) obj;
    }
}
