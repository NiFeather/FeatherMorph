package xyz.nifeather.morph.config;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
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
import java.util.function.Consumer;

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

                options.put(val.node().toString(), val);
            }
            catch (Throwable t)
            {
                logger.warn("Failed to discover configuration %s, this don't seems right!".formatted(field.getName()), t);
            }
        }

        registerUpdateMethods();
    }

    @SuppressWarnings("removal")
    private void registerUpdateMethods()
    {
        var updateLogger = LoggerFactory.getLogger("FeatherMorph$ConfigUpdate");

        addUpdateMethod(0, newConfig ->
        {
            updateLogger.info("#0: Determining language code to use.");
            var locale = Locale.getDefault().toLanguageTag().replace('-', '_').toLowerCase();
            newConfig.set(ConfigOptions.LANGUAGE_CODE.toString(), locale);
            newConfig.set(ConfigOptions.VERSION.toString(), 1);
        });

        addUpdateMethod(14, newConfig ->
        {
            updateLogger.info("#14: Checking for old action item key.");

            //skill item
            var oldSkillItem = get(ConfigOptions.ACTION_ITEM);

            //noinspection removal
            this.remove(ConfigOptions.ACTION_ITEM);

            if (oldSkillItem != null)
            {
                //noinspection removal
                newConfig.set(ConfigOptions.SKILL_ITEM.toString(), oldSkillItem);
            }
        });

        addUpdateMethod(20, newConfig ->
        {
            updateLogger.info("#20: ChatOverride configuration is now in config.yml.");

            // ChatOverride消息的配置从messages迁移到config.yml中
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
        });

        addUpdateMethod(22, newConfig ->
        {
            updateLogger.info("#22: The old *Modify bounding boxes* config has moved to a new section");

            //noinspection removal
            var val = getOrDefault(ConfigOptions.MODIFY_BOUNDING_BOX_LEGACY, null);

            //noinspection removal
            this.remove(ConfigOptions.MODIFY_BOUNDING_BOX_LEGACY);

            if (val != null)
                newConfig.set(ConfigOptions.MODIFY_BOUNDING_BOX.toString(), val);
        });

        addUpdateMethod(33, newConfig ->
        {
            updateLogger.info("#33: Flying in water/lava has been changed from no_fly_in_liquid -> disallow_in_water/disallow_in_lava.");

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
        });

        addUpdateMethod(36, newConfig ->
        {
            updateLogger.info("#36: Removing old skill item config.");

            //noinspection removal
            this.remove(ConfigOptions.SKILL_ITEM);
        });

        addUpdateMethod(39, newConfig ->
        {
            updateLogger.info("#39: Remove the 'show_player_disguises_in_tab' and 'hide_disguised_players_in_tab' options.");

            //noinspection removal
            this.remove(ConfigOptions.SR_SHOW_PLAYER_DISGUISES_IN_TAB);
            this.remove(ConfigOptions.HIDE_DISGUISED_PLAYERS_IN_TAB);
        });

        addUpdateMethod(40, newConfig ->
        {
            updateLogger.info("#40: The sentry logger has been removed since it's not working right, and I don't like to bother about it as we lose 2 follows because of adding it D:");

            this.remove(ConfigOptions.ENABLE_SENTRY_LOGGER);
        });

        addUpdateMethod(43, newConfig ->
        {
            updateLogger.info("#43: Blacklist patterns and tags have been removed as they are no longer being used.");

            this.remove(ConfigOptions.BLACKLIST_PATTERNS);
            this.remove(ConfigOptions.BLACKLIST_TAGS);
        });

        addUpdateMethod(45, newConfig ->
        {
            updateLogger.info("#45: AI Modification and Interaction Mirror is now removed and planned to implement in a separate project");

            //todo: Calling `remove` still leave non-default values in the config file, wtf
            this.remove(ConfigOptions.DO_MODIFY_AI);

            this.remove(ConfigOptions.MIRROR_CONTROL_DISTANCE);
            this.remove(ConfigOptions.MIRROR_IGNORE_DISGUISED);
            this.remove(ConfigOptions.MIRROR_DESTROY_TIMEOUT);
            this.remove(ConfigOptions.MIRROR_LOG_OPERATION);
            this.remove(ConfigOptions.MIRROR_LOG_CLEANUP_DATE);
            this.remove(ConfigOptions.MIRROR_SELECTION_MODE);

            this.remove(ConfigOptions.MIRROR_BEHAVIOR_DO_SIMULATION);
            this.remove(ConfigOptions.MIRROR_BEHAVIOR_DROP);
            this.remove(ConfigOptions.MIRROR_BEHAVIOR_HOTBAR);
            this.remove(ConfigOptions.MIRROR_BEHAVIOR_SNEAK);
            this.remove(ConfigOptions.MIRROR_BEHAVIOR_SWAP_HAND);
        });
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

    private void ensureBindableListNotNull()
    {
        if (bindableLists == null)
            bindableLists = new Object2ObjectOpenHashMap<>();
    }

    private final Map<Integer, Consumer<FileConfiguration>> updateMethods = new ConcurrentHashMap<>();

    private void addUpdateMethod(int startingAt, Consumer<FileConfiguration> runnable)
    {
        updateMethods.put(startingAt, runnable);
    }

    /**
     * Update methods for version [startingFrom, ∞)
     *
     * @param startingFrom The version number to start applying update method.
     */
    public List<Consumer<FileConfiguration>> getUpdateMethods(int startingFrom)
    {
        return updateMethods.entrySet().stream()
                .filter(entry -> entry.getKey() >= startingFrom)
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .toList();
    }

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
        int targetVersion = 46;

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

            this.getUpdateMethods(configVersion).forEach(c -> c.accept(newConfig));
            newConfig.set(ConfigOptions.VERSION.toString(), targetVersion);

            plugin.saveConfig();
            reload();
        }

        generateIfUnset(ConfigOptions.MASTER_SECRET);
        generateIfUnset(ConfigOptions.UUID_RANDOM_BASE);
    }

    private void generateIfUnset(ConfigOption<String> option)
    {
        if (!(getOrDefault(option, "~UNSET")).equalsIgnoreCase("~UNSET"))
            return;

        set(option, RandomStringUtils.secure().nextAlphabetic(20));
    }

    public void remove(ConfigOption<?> option, FileConfiguration backendConfig)
    {
        this.set(option, null);
        backendConfig.set(option.node().toString(), null);
    }

    public void remove(ConfigOption<?> option)
    {
        remove(option, this.backendConfig);
    }

    public <T> T get(ConfigOption<T> option)
    {
        return get(option.type(), option.node());
    }
}