package xiamomc.morph.misc.recipe;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.configuration.file.YamlConfiguration;
import xiamomc.morph.MorphPluginObject;
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Configuration.ConfigOption;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class StandaloneYamlConfigManager extends MorphPluginObject
{
    private YamlConfiguration backendConfiguration;

    private final File file;

    public StandaloneYamlConfigManager(File file)
    {
        this.file = file;

        reload();
    }

    /**
     * Copy internal resource to the location
     * @return Whether this operation was successful
     */
    protected abstract boolean copyInternalResource();

    public void reload()
    {
        var newConfig = new YamlConfiguration();

        if (!file.exists())
        {
            if (!copyInternalResource())
            {
                logger.error("Can't create file to save configuration! Not reloading recipes...");
                return;
            }
        }

        try
        {
            newConfig.load(file);
        }
        catch (Throwable e)
        {
            logger.error("Unable to load recipe configuration: " + e.getMessage());
            return;
        }

        this.backendConfiguration = newConfig;
    }

    private final Map<String, Bindable<?>> bindableMap = new ConcurrentHashMap<>();

    public <T> Bindable<T> getBindable(ConfigOption<T> option)
    {
        var cache = bindableMap.get(option.toString());
        if (cache != null) return (Bindable<T>) cache;

        if (Map.class.isAssignableFrom(option.getDefault().getClass()))
            throw new IllegalArgumentException("Maps cannot being used with Bindable");

        var value = this.getOrDefault(option, option.getDefault());

        var bindable = new Bindable<T>(value);
        bindableMap.put(option.toString(), bindable);

        return bindable;
    }

    public <T> T get(ConfigOption<T> option)
    {
        return getOrDefault(option, null);
    }

    /**
     * @apiNote List classes will ALWAYS return ArrayList
     */
    public <T> T getOrDefault(ConfigOption<T> option, T defaultVal)
    {
        var node = option.toString();

        Object backendResult = backendConfiguration.get(node, defaultVal);
        if (backendResult == null && defaultVal == null) return null;

        var optionDefault = option.getDefault();

        // 对列表单独处理
        if (List.class.isAssignableFrom(optionDefault.getClass()))
        {
            var elementClass = optionDefault.getClass();
            var newResult = backendConfiguration.getList(node, new ArrayList<>());
            newResult.removeIf((listVal) -> {
                return !elementClass.isInstance(listVal);
            });

            return (T) newResult;
        }
        else
        {
            // ConfigOption#getDefault is always NotNull
            if (optionDefault.getClass().isAssignableFrom(backendResult.getClass()))
                return (T) backendResult;
            else
                return defaultVal;
        }
    }
}
