package xiamomc.morph.config;

import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Configuration.ConfigNode;
import xiamomc.pluginbase.Configuration.PluginConfigManager;
import xiamomc.pluginbase.XiaMoJavaPlugin;

public class MorphConfigManager extends PluginConfigManager
{
    public MorphConfigManager(XiaMoJavaPlugin plugin)
    {
        super(plugin);
    }

    public <T> T getOrDefault(Class<T> type, ConfigNode node, @Nullable T defaultValue)
    {
        var val = super.get(type, node);

        if (val == null)
        {
            set(node, defaultValue);
            return defaultValue;
        }

        return val;
    }
}
