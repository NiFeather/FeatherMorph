package xiamomc.morph.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.MorphPluginObject;
import xiamomc.pluginbase.Annotations.Initializer;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class JsonBasedStorage<T> extends MorphPluginObject
{
    private File configurationFile;

    private final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    protected JsonBasedStorage()
    {
        storingObject = createDefault();
    }

    @Initializer
    private void load(MorphPlugin plugin) throws IOException
    {
        //初始化配置文件
        if (configurationFile == null)
            configurationFile = new File(URI.create(plugin.getDataFolder().toURI() + "/" + getFileName()));

        if (!configurationFile.exists())
        {
            //创建父目录
            if (!configurationFile.getParentFile().exists())
                Files.createDirectories(Paths.get(configurationFile.getParentFile().toURI()));

            if (!configurationFile.createNewFile())
            {
                Logger.error("未能创建文件，将不会加载" + getDisplayName() + "的JSON配置！");
                return;
            }
        }

        reloadConfiguration();
    }

    @NotNull
    protected abstract String getFileName();

    @NotNull
    protected abstract T createDefault();

    @NotNull
    protected abstract String getDisplayName();

    protected T storingObject;

    public boolean reloadConfiguration()
    {
        //加载JSON配置
        Object targetStore = null;
        var success = false;

        //从文件读取并反序列化为配置
        try (var jsonStream = new InputStreamReader(new FileInputStream(configurationFile)))
        {
            targetStore = gson.fromJson(jsonStream, storingObject.getClass());
            success = true;
        }
        catch (Exception e)
        {
            Logger.warn("无法加载" + getDisplayName() + "的JSON配置：" + e.getMessage());
            e.printStackTrace();
        }

        //确保targetStore不是null
        if (targetStore == null) targetStore = createDefault();

        //设置并保存
        storingObject = (T) targetStore;

        saveConfiguration();

        return success;
    }

    public boolean saveConfiguration()
    {
        try
        {
            var jsonString = gson.toJson(storingObject);

            if (configurationFile.exists()) configurationFile.delete();

            if (!configurationFile.createNewFile())
            {
                Logger.error("未能创建文件，将不会保存" + getDisplayName() + "的配置！");
                return false;
            }

            try (var stream = new FileOutputStream(configurationFile))
            {
                stream.write(jsonString.getBytes());
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        return true;
    }
}
