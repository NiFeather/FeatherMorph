package xiamomc.morph.config;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import xiamomc.morph.MorphPlugin;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Bindables.BindableList;
import xiamomc.pluginbase.Configuration.ConfigNode;
import xiamomc.pluginbase.Configuration.PluginConfigManager;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MorphConfigManager extends PluginConfigManager
{
    public MorphConfigManager(MorphPlugin plugin)
    {
        super(plugin);

        instance = this;
    }

    private static MorphConfigManager instance;

    public static MorphConfigManager getInstance()
    {
        return instance;
    }

    public <T> T getOrDefault(Class<T> type, ConfigOption option)
    {
        var val = get(type, option);

        if (val == null)
        {
            set(option, option.defaultValue);
            return (T) option.defaultValue;
        }

        return val;
    }

    public <T> T getOrDefault(Class<T> type, ConfigOption option, @Nullable T defaultValue)
    {
        var val = get(type, option);

        if (val == null)
        {
            set(option, defaultValue);
            return defaultValue;
        }

        return val;
    }

    @NotNull
    @Override
    public Map<ConfigNode, Object> getAllNotDefault()
    {
        var options = ConfigOption.values();
        var map = new Object2ObjectOpenHashMap<ConfigNode, Object>();

        for (var o : options)
        {
            var val = getOrDefault(Object.class, o);

            if (!val.equals(o.defaultValue)) map.put(o.node, val);
        }

        return map;
    }

    public <T> BindableList<T> getBindableList(Class<T> type, ConfigOption option)
    {
        var originalBindable = getBindable(List.class, option.node, List.of());
        var list = new BindableList<T>(originalBindable.get());

        originalBindable.onValueChanged((o, n) ->
        {
            list.clear();
            list.addAll(n);
        });

        return list;
    }

    public <T> Bindable<T> getBindable(Class<T> type, ConfigOption option)
    {
        if (type.isInstance(option.defaultValue))
            return getBindable(type, option, (T)option.defaultValue);

        throw new IllegalArgumentException(option + "的类型和" + type + "不兼容");
    }

    public <T> void bind(Bindable<T> bindable, ConfigOption option)
    {
        var bb = this.getBindable(option.defaultValue.getClass(), option);

        if (bindable.getClass().isInstance(bb))
            bindable.bindTo((Bindable<T>) bb);
        else
            throw new IllegalArgumentException("尝试将一个Bindable绑定在不兼容的配置(" + option + ")上");
    }

    public <T> Bindable<T> getBindable(Class<T> type, ConfigOption path, T defaultValue)
    {
        return super.getBindable(type, path.node, defaultValue);
    }

    @Override
    public void reload()
    {
        super.reload();

        //更新配置
        int targetVersion = 15;

        var configVersion = getOrDefault(Integer.class, ConfigOption.VERSION);

        if (configVersion < targetVersion)
        {
            var nonDefaults = this.getAllNotDefault();

            plugin.saveResource("config.yml", true);
            plugin.reloadConfig();

            var newConfig = plugin.getConfig();

            nonDefaults.forEach((n, v) -> newConfig.set(n.toString(), v));

            //初次加载
            if (configVersion < 1)
            {
                var locale = Locale.getDefault().toLanguageTag().replace('-', '_').toLowerCase();
                newConfig.set(ConfigOption.LANGUAGE_CODE.toString(), locale);
            }

            if (configVersion < 15)
            {
                //skill item
                //noinspection deprecation
                var oldSkillItem = get(String.class, ConfigOption.ACTION_ITEM);
                newConfig.set(ConfigOption.SKILL_ITEM.toString(), oldSkillItem);
            }

            newConfig.set(ConfigOption.VERSION.toString(), targetVersion);

            plugin.saveConfig();
            reload();
        }
    }

    public <T> T get(Class<T> type, ConfigOption option)
    {
        return get(type, option.node);
    }

    public void set(ConfigOption option, Object val)
    {
        this.set(option.node, val);
    }
}