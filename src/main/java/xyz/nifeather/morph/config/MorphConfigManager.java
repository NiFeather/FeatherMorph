package xyz.nifeather.morph.config;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Bindables.BindableList;
import xiamomc.pluginbase.Configuration.ConfigNode;
import xiamomc.pluginbase.Configuration.ConfigOption;
import xiamomc.pluginbase.Configuration.PluginConfigManager;
import xiamomc.pluginbase.Managers.DependencyManager;
import xiamomc.pluginbase.Messages.MessageStore;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.messages.strings.CommonStrings;
import xyz.nifeather.morph.messages.MessageUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MorphConfigManager extends PluginConfigManager
{
    public MorphConfigManager(FeatherMorphMain plugin)
    {
        super(plugin);

        instance = this;

        var logger = FeatherMorphMain.getInstance().getSLF4JLogger();
        for (Field field : ConfigOptions.class.getFields())
        {
            if (field.getType() != ConfigOption.class) continue;

            try
            {
                var val = (ConfigOption<?>) field.get(null);
                logger.debug("Discover field %s --> %s".formatted(field.getName(), val.node()));
            }
            catch (Throwable t)
            {
                logger.warn("Failed to discover configuration %s, this don't seems right!".formatted(field.getName()), t);
            }
        }
    }

    private static MorphConfigManager instance;

    public static MorphConfigManager getInstance()
    {
        return instance;
    }

    private final Map<String, ConfigOption<?>> options = new ConcurrentHashMap<>();

    @NotNull
    @Override
    public Map<ConfigNode, Object> getAllNotDefault()
    {
        var map = new Object2ObjectOpenHashMap<ConfigNode, Object>();

        for (var o : options.values())
        {
            var val = getOrDefault(o);

            if (!val.equals(o.getDefault())) map.put(o.node(), val);
        }

        return map;
    }

    private Map<String, BindableList<?>> bindableLists;

    public <T> BindableList<T> getBindableStringList(ConfigOption<List<T>> option)
    {
        ensureBindableListNotNull();

        var val = bindableLists.getOrDefault(option.toString(), null);
        if (val != null) return (BindableList<T>) val;

        List<?> originalList = backendConfig.getList(option.toString(), new ArrayList<T>());
        originalList.removeIf(listVal -> !option.type().isInstance(listVal)); //Don't work for somehow

        var list = new BindableList<T>();
        list.addAll((List<T>)originalList);

        list.onListChanged((diffList, reason) ->
        {
            backendConfig.set(option.toString(), new ArrayList<>(list));
            save();
        }, true);

        bindableLists.put(option.toString(), list);

        return list;
    }


    public <T> Bindable<T> getBindable(ConfigOption<T> option, T defaultValue)
    {
        return super.getBindable(option.type(), option.node(), defaultValue);
    }

    private void ensureBindableListNotNull()
    {
        if (bindableLists == null)
            bindableLists = new Object2ObjectOpenHashMap<>();
    }

    @SuppressWarnings("removal")
    @Override
    public void reload()
    {
        super.reload();

        ensureBindableListNotNull();
        bindableLists.forEach((node, list) ->
        {
            var configList = backendConfig.getList(node);
            if (configList == null)
                return;

            list.clear();
            list.addAllInternal(configList);
        });

        //更新配置
        int targetVersion = 44;

        var configVersion = getOrDefault(ConfigOptions.VERSION);

        if (configVersion < targetVersion)
        {
            FeatherMorphMain.getInstance().getSLF4JLogger().info("Migrating config from %s to %s".formatted(configVersion, targetVersion));

            var nonDefaults = this.getAllNotDefault();

            plugin.saveResource("config.yml", true);
            plugin.reloadConfig();

            var newConfig = plugin.getConfig();

            nonDefaults.forEach((n, v) ->
            {
                var matching = options.getOrDefault(n.toString(), null);

                if (matching == null)
                {
                    //MorphPlugin.getInstance().getSLF4JLogger().warn("Null ConfigOption for node '%s', skipping...".formatted(n));
                    return;
                }

                //noinspection rawtypes
                if (v instanceof Collection collection)
                {
                    Collection<?> defaultVal = null;

                    if (matching.getDefault() instanceof Collection<?> c1)
                        defaultVal = c1;

                    if (defaultVal != null)
                    {
                        defaultVal.forEach(c ->
                        {
                            if (!collection.contains(c))
                                collection.add(c);
                        });
                    }

                    newConfig.set(n.toString(), v);
                }
                else
                {
                    newConfig.set(n.toString(), v);
                }
            });

            // 初次加载
            if (configVersion < 1)
            {
                var locale = Locale.getDefault().toLanguageTag().replace('-', '_').toLowerCase();
                newConfig.set(ConfigOptions.LANGUAGE_CODE.toString(), locale);
            }

            if (configVersion < 15)
            {
                //skill item
                var oldSkillItem = get(ConfigOptions.ACTION_ITEM);

                //noinspection removal
                this.remove(ConfigOptions.ACTION_ITEM);

                if (oldSkillItem != null)
                {
                    //noinspection removal
                    newConfig.set(ConfigOptions.SKILL_ITEM.toString(), oldSkillItem);
                }
            }

            // ChatOverride消息的配置从messages迁移到config.yml中
            if (configVersion < 21)
            {
                var depMgr = DependencyManager.getInstance(plugin.getNamespace());
                var messageStore = depMgr.get(MessageStore.class);
                boolean requireCache = depMgr.get(this.getClass(), false) == null;

                if (requireCache)
                    depMgr.cache(this);

                var msg = messageStore.get(CommonStrings.chatOverrideDefaultPattern().getKey(), ConfigOptions.CHAT_OVERRIDE_DEFAULT_PATTERN.getDefault(), MessageUtils.getServerLocale());
                newConfig.set(ConfigOptions.CHAT_OVERRIDE_DEFAULT_PATTERN.toString(), msg);

                var pluginPrefix = messageStore.get(CommonStrings.pluginMessageString().getKey(), ConfigOptions.PLUGIN_PREFIX.getDefault(), MessageUtils.getServerLocale());
                newConfig.set(ConfigOptions.PLUGIN_PREFIX.toString(), pluginPrefix);

                if (requireCache)
                    depMgr.unCache(this);
            }

            if (configVersion < 23)
            {
                //noinspection removal
                var val = get(ConfigOptions.MODIFY_BOUNDING_BOX_LEGACY);

                //noinspection removal
                this.remove(ConfigOptions.MODIFY_BOUNDING_BOX_LEGACY);

                if (val != null)
                    newConfig.set(ConfigOptions.MODIFY_BOUNDING_BOX.toString(), val);
            }

            if (configVersion < 34)
            {
                //noinspection removal
                var noFlyInLiquid = getOrDefault(ConfigOptions.FLYABILITY_NO_LIQUID, false);

                //noinspection removal
                this.remove(ConfigOptions.FLYABILITY_NO_LIQUID);

                if (noFlyInLiquid)
                {
                    var list = Bukkit.getWorlds().stream().map(WorldInfo::getName).toList();

                    newConfig.set(ConfigOptions.FLYABILITY_DISALLOW_FLY_IN_WATER.toString(), list);
                    newConfig.set(ConfigOptions.FLYABILITY_DISALLOW_FLY_IN_LAVA.toString(), list);
                }
            }

            if (configVersion < 37)
            {
                //noinspection removal
                this.remove(ConfigOptions.SKILL_ITEM);
            }

            if (configVersion < 40)
            {
                //noinspection removal
                this.remove(ConfigOptions.SR_SHOW_PLAYER_DISGUISES_IN_TAB);
                this.remove(ConfigOptions.HIDE_DISGUISED_PLAYERS_IN_TAB);
            }

            if (configVersion < 41)
            {
                this.remove(ConfigOptions.ENABLE_SENTRY_LOGGER);
            }

            /*if (configVersion < 43 && FoliaThreadUtils.isFolia())
            {
                newConfig.set(ConfigOptions.DO_MODIFY_AI.node.toString(), false);
                FeatherMorphMain.getInstance().getSLF4JLogger().info("AI Modification has been disabled due to having issue on Folia server");
            }*/

            if (configVersion < 44)
            {
                this.remove(ConfigOptions.BLACKLIST_PATTERNS);
                this.remove(ConfigOptions.BLACKLIST_TAGS);
            }

            newConfig.set(ConfigOptions.VERSION.toString(), targetVersion);

            //todo: 将~UNSET作为留空的保留字符串写入PluginBase
            if (((String)newConfig.get(ConfigOptions.MASTER_SECRET.toString(), "~UNSET")).equalsIgnoreCase("~UNSET"))
            {
                var defVal = ConfigOptions.MASTER_SECRET.getDefault().toString();

                getBindable(ConfigOptions.MASTER_SECRET).set(defVal);
                newConfig.set(ConfigOptions.MASTER_SECRET.toString(), defVal);
            }

            plugin.saveConfig();
            reload();
        }
    }

    public void remove(ConfigOption<?> option)
    {
        this.set(option, null);
        this.backendConfig.set(option.node().toString(), null);
    }

    public <T> T get(ConfigOption<T> option)
    {
        return get(option.type(), option.node());
    }
}