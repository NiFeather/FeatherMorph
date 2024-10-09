package xyz.nifeather.morph.misc.recipe;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.MorphPluginObject;
import xiamomc.pluginbase.Configuration.ConfigNode;
import xiamomc.pluginbase.Configuration.ConfigOption;

import java.io.File;
import java.util.*;

public abstract class StandaloneYamlConfigManager extends MorphPluginObject
{
    protected YamlConfiguration backendConfiguration;

    @NotNull
    protected final File configFile;

    @Nullable
    private final String internalResourceName;

    public static final ConfigOption<Integer> CONFIG_VERSION = new ConfigOption<>(ConfigNode.create().append("version"), 0);

    public StandaloneYamlConfigManager(@NotNull File file, @Nullable String internalResourceName)
    {
        this.configFile = file;
        this.internalResourceName = internalResourceName;
    }

    /**
     * Copy internal resource to the location
     * @return Whether this operation was successful
     */
    protected abstract boolean copyInternalResource();

    protected abstract int getExpectedConfigVersion();

    public void reload()
    {
        var newConfig = new YamlConfiguration();

        if (!configFile.exists())
        {
            if (!copyInternalResource())
            {
                logger.error("Can't create file to save configuration! Not reloading recipes...");
                return;
            }
        }

        try
        {
            newConfig.load(configFile);
        }
        catch (Throwable e)
        {
            logger.error("Unable to load recipe configuration: " + e.getMessage());
            return;
        }

        if (this.backendConfiguration == null)
            this.backendConfiguration = newConfig;

        var configVersion = newConfig.getInt(CONFIG_VERSION.toString(), 0);
        if (configVersion < this.getExpectedConfigVersion())
            this.migrate(this.backendConfiguration, newConfig);

        this.backendConfiguration = newConfig;
    }

    @NotNull
    protected Map<ConfigNode, Object> getAllNotDefault(Collection<ConfigOption<?>> options)
    {
        var map = new Object2ObjectOpenHashMap<ConfigNode, Object>();

        for (var o : options)
        {
            Object val;

            if (o.getDefault() instanceof List<?>)
                val = getList(o);
            else if (o.getDefault() instanceof Map<?, ?>)
                val = getMap(o);
            else
                val = getOrDefault((ConfigOption<Object>) o, o.getDefault());

            if (!o.getDefault().equals(val)) map.put(o.node(), val);
        }

        return map;
    }

    protected abstract List<ConfigOption<?>> getAllOptions();

    private void migrate(@Nullable YamlConfiguration currentConfig, YamlConfiguration newConfig)
    {
        var allNotDefault = this.getAllNotDefault(this.getAllOptions());

        if (internalResourceName != null)
            plugin.saveResource(internalResourceName, true);

        this.onMigrate(currentConfig, newConfig, allNotDefault);

        allNotDefault.forEach((node, val) ->
        {
            var matching = this.getAllOptions().stream().filter(option -> option.node().equals(node))
                    .findFirst().orElse(null);

            if (matching == null)
                return;

            newConfig.set(node.toString(), val);
        });
    }

    protected void onMigrate(@Nullable YamlConfiguration currentConfig, YamlConfiguration newConfig, Map<ConfigNode, Object> nonDefaultValues)
    {
    }

    /**
     * @return NULL if not found
     */
    public <T> T get(ConfigOption<T> option)
    {
        return getOrDefault(option, null);
    }

    @NotNull
    public List<String> getList(ConfigOption<?> option)
    {
        var node = option.toString();
        return backendConfiguration.getStringList(node);
    }

    /**
     * @return NULL if the given node doesn't exist in the configuration
     */
    @Nullable
    public Map<String, String> getMap(ConfigOption<?> option)
    {
        var node = option.toString();

        var configSection = backendConfiguration.getConfigurationSection(node);

        if (configSection == null)
            return null;

        Map<String, String> map = new Object2ObjectOpenHashMap<>();

        configSection.getKeys(false).forEach(key -> map.put(key, configSection.getString(key)));

        return map;
    }

    public <T> T getOrDefault(ConfigOption<T> option)
    {
        return getOrDefault(option, option.getDefault());
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
            throw new IllegalArgumentException("Use getList() instead.");
        }
        else if (Map.class.isAssignableFrom(optionDefault.getClass()))
        {
            throw new IllegalArgumentException("Use getMap() instead.");
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
