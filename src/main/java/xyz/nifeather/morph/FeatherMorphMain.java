package xyz.nifeather.morph;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Messages.MessageStore;
import xiamomc.pluginbase.ScheduleInfo;
import xiamomc.pluginbase.XiaMoJavaPlugin;
import xyz.nifeather.morph.abilities.AbilityManager;
import xyz.nifeather.morph.api.FeatherMorphAPI;
import xyz.nifeather.morph.api.networking.exceptions.PluginDisabledException;
import xyz.nifeather.morph.commands.MorphCommandManager;
import xyz.nifeather.morph.config.ConfigOption;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.events.*;
import xyz.nifeather.morph.events.mirror.ExecutorHub;
import xyz.nifeather.morph.interfaces.IManagePlayerData;
import xyz.nifeather.morph.interfaces.IManageRequests;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.MorphMessageStore;
import xyz.nifeather.morph.messages.vanilla.VanillaMessageStore;
import xyz.nifeather.morph.misc.ModNetworkingHelper;
import xyz.nifeather.morph.misc.PlayerOperationSimulator;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.gui.IconLookup;
import xyz.nifeather.morph.misc.integrations.placeholderapi.PlaceholderIntegration;
import xyz.nifeather.morph.misc.integrations.residence.ResidenceEventProcessor;
import xyz.nifeather.morph.misc.integrations.towny.TownyAdapter;
import xyz.nifeather.morph.misc.recipe.RecipeManager;
import xyz.nifeather.morph.network.multiInstance.MultiInstanceService;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.platform.CurrentPlatform;
import xyz.nifeather.morph.platform.impl.paper.PaperPlatform;
import xyz.nifeather.morph.skills.SkillManager;
import xyz.nifeather.morph.storage.skill.SkillsConfigurationStoreNew;
import xyz.nifeather.morph.updates.UpdateHandler;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public final class FeatherMorphMain extends XiaMoJavaPlugin
{
    private static FeatherMorphMain instance;
    private final Bindable<Boolean> debugOutput = new Bindable<>(false);

    public PaperPlatform getPlatform()
    {
        return CurrentPlatform.instance();
    }

    /**
     * 仅当当前对象无法继承MorphPluginObject或不需要完全继承MorphPluginObject时使用
     * @return 插件的实例
     */
    public static FeatherMorphMain getInstance()
    {
        return instance;
    }

    public FeatherMorphMain()
    {
        instance = this;

        CurrentPlatform.instance();

        boolean folia = false;
        try
        {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");

            logger.info("We are running on a Folia server!");
            folia = true;
        }
        catch (Throwable ignored)
        {
            logger.info("io.papermc.paper.threadedregions.RegionizedServer not found, possibly not a Folia server.");
        }

        isFolia = folia;
    }

    public static String getMorphNameSpace()
    {
        return "morphplugin";
    }

    @Override
    public String getNamespace()
    {
        return getMorphNameSpace();
    }

    public boolean debugOutputEnabled()
    {
        return debugOutput.get();
    }

    private MorphCommandManager cmdHelper;

    private MorphManager morphManager;

    private PluginManager pluginManager;

    private SkillManager skillHandler;

    private AbilityManager abilityManager;

    private VanillaMessageStore vanillaMessageStore;

    private MorphMessageStore messageStore;

    private PlaceholderIntegration placeholderIntegration;

    private MorphClientHandler clientHandler;

    private Metrics metrics;

    private MultiInstanceService instanceService;

    @Nullable
    private EntityProcessor entityProcessor;

    private ExecutorHub mirrorExecutorHub;

    private final boolean isFolia;
    public boolean isFolia()
    {
        return isFolia;
    }

    private static final String noticeHeaderFooter = "- x - x - x - x - x - x - x - x - x - x - x - x -";
    private void printImportantWarning(boolean critical, String... warnings)
    {
        if (critical)
        {
            logger.error(noticeHeaderFooter);
            logger.error("");

            for (String warning : warnings)
                logger.error(warning);

            logger.error("");
            logger.error(noticeHeaderFooter);
        }
        else
        {
            logger.warn(noticeHeaderFooter);
            logger.warn("");

            for (String warning : warnings)
                logger.warn(warning);

            logger.warn("");
            logger.warn(noticeHeaderFooter);
        }
    }

    @ApiStatus.Internal
    public int execSchedules = 0;

    @Override
    public ScheduleInfo schedule(Runnable function, int delay, boolean async)
    {
        execSchedules++;
        return super.schedule(function, delay, async);
    }

    @ApiStatus.Internal
    public static void panic(String... message)
    {
        var plugin = FeatherMorphMain.getInstance();
        var logger = plugin.getSLF4JLogger();

        logger.error("- x - x - x - x - x - x - x - x - x - x - x - x - x - x - x -");
        logger.error("PANIC!");
        for (String s : message)
        {
            logger.error(s);
        }

        logger.error("- x - x - x - x - x - x - x - x - x - x - x - x - x - x - x -");
        logger.error("Called at {}", Thread.currentThread().getName());
        logger.error("Invoke stacktrace:");
        Thread.dumpStack();
        logger.error("- x - x - x - x - x - x - x - x - x - x - x - x - x - x - x -");

        FeatherMorphAPI.panic();

        if (plugin.isEnabled())
            Bukkit.getPluginManager().disablePlugin(FeatherMorphMain.getInstance());
    }

    @Override
    protected void enable()
    {
        super.enable();

        pluginManager = Bukkit.getPluginManager();
        var bukkitVersion = Bukkit.getMinecraftVersion();

        String primaryVersion = "1.21.8";
        String[] compatVersions = new String[] { primaryVersion };
        if (Arrays.stream(compatVersions).noneMatch(bukkitVersion::equals))
        {
            printImportantWarning(
                    true,
                    "This version of Minecraft (%s) is not supported!".formatted(bukkitVersion),
                    "Please use %s instead!".formatted(primaryVersion)
            );

            panic("This version of Minecraft is not supported.");
            return;
        }

        if (!bukkitVersion.equals(primaryVersion))
        {
            printImportantWarning(
                    false,
                    "Minecraft %s is not primary supported!".formatted(bukkitVersion),
                    "We suggest to use %s instead!".formatted(primaryVersion)
            );
        }

        this.metrics = new Metrics(this, 18062);

        this.registerListener(softDeps);

        var playerTracker = new PlayerTracker();

        softDeps.setHandle("PlaceholderAPI", p ->
        {
            logger.info("Registering Placeholders...");
            placeholderIntegration = new PlaceholderIntegration(dependencyManager);
            placeholderIntegration.register();
        }, true);

        softDeps.setHandle("Residence", r ->
        {
            logger.info("Residence detected, applying integrations...");
            this.registerListener(new ResidenceEventProcessor());
        }, true);

        softDeps.setHandle("Towny", plugin ->
        {
            logger.info("Towny detected, applying integrations...");
            this.registerListener(new TownyAdapter());
        }, true);

        //缓存依赖
        dependencyManager.cache(this);
        dependencyManager.cache(clientHandler = new MorphClientHandler());
        dependencyManager.cache(new ModNetworkingHelper());

        dependencyManager.cache(morphManager = new MorphManager());
        dependencyManager.cache(skillHandler = new SkillManager());
        dependencyManager.cache(abilityManager = new AbilityManager());
        dependencyManager.cache(new RevealingHandler());

        dependencyManager.cache(vanillaMessageStore = new VanillaMessageStore());

        MorphConfigManager config;
        dependencyManager.cacheAs(MessageStore.class, messageStore = new MorphMessageStore());
        dependencyManager.cacheAs(MiniMessage.class, MiniMessage.miniMessage());
        dependencyManager.cacheAs(IManagePlayerData.class, morphManager);
        dependencyManager.cacheAs(IManageRequests.class, new RequestManager());
        dependencyManager.cacheAs(Scoreboard.class, Bukkit.getScoreboardManager().getMainScoreboard());
        dependencyManager.cacheAs(MorphConfigManager.class, config = new MorphConfigManager(this));
        dependencyManager.cache(playerTracker);

        config.bind(debugOutput, ConfigOption.DEBUG_OUTPUT);

        dependencyManager.cache(cmdHelper = new MorphCommandManager());

        dependencyManager.cache(new SkillsConfigurationStoreNew());

        dependencyManager.cache(new MessageUtils());

        dependencyManager.cache(new PlayerOperationSimulator());

        var updateHandler = new UpdateHandler();
        dependencyManager.cache(updateHandler);

        dependencyManager.cache(instanceService = new MultiInstanceService());

        dependencyManager.cache(DisguiseProperties.INSTANCE);

        dependencyManager.cache(new RecipeManager());

        dependencyManager.cache(mirrorExecutorHub = new ExecutorHub());

        var mirrorProcessor = new InteractionMirrorProcessor();

        // Commands
        var lifecycleManager = this.getLifecycleManager();
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, event ->
                cmdHelper.register(event));

        var listeners = new Listener[]
                {
                        playerTracker,
                        mirrorProcessor,
                        new CommonEventProcessor(),
                        new CustomItemRelatedEvents(),
                        new RevealingEventProcessor(),
                        new DisguiseAnimationProcessor(),
                        new ForcedDisguiseProcessor(),
                        new PlayerSkinProcessor(),
                        new WorkaroundProcessor(),
                        entityProcessor = new EntityProcessor()
                };

        //注册EventProcessor
        this.schedule(() ->
        {
            registerListeners(listeners);

            dependencyManager.cache(new FeatherMorphAPI(this));
        });

        pluginEnableDone.set(true);

        //Init GUI IconLookup
        IconLookup.instance();
    }

    private final AtomicBoolean pluginEnableDone = new AtomicBoolean(false);

    @Override
    public void disable()
    {
        var serverStopping = getServer().isStopping();
        if (!serverStopping)
        {
            if (pluginEnableDone.get())
            {
                printImportantWarning(true,
                        "HEY, THERE!",
                        "Are you doing a hot reload?",
                        "Note that FeatherMorph does NOT support doing such!",
                        "Before you open any issues, please do a FULL RESTART for your server! We will NOT provide any support after the hot reload!");
            }
        }

        FeatherMorphBootstrap.pluginDisabled.set(true);

        //调用super.onDisable后依赖管理器会被清空
        //需要在调用前先把一些东西处理好
        try
        {
            if (entityProcessor != null
                    && !serverStopping
                    && entityProcessor.currentlyDoModifyAI()
                    && pluginEnableDone.get())
            {
                printImportantWarning(true,
                        "Disabling/reloading FeatherMorph while modifying AI is not supported",
                        "Expect problems!");
            }

            if (morphManager != null)
                morphManager.onPluginDisable();

            if (placeholderIntegration != null)
                placeholderIntegration.unregister();

            if (clientHandler != null)
                clientHandler.getConnectedPlayers().forEach(p -> clientHandler.disconnect(p, new PluginDisabledException("Plugin has been disabled")));

            if (metrics != null)
                metrics.shutdown();

            if (mirrorExecutorHub != null)
                mirrorExecutorHub.pushToLoggingBase();

            if (instanceService != null)
                instanceService.onDisable();

            var messenger = this.getServer().getMessenger();

            messenger.unregisterOutgoingPluginChannel(this);
        }
        catch (Exception e)
        {
            logger.warn("Error occurred while disabling", e);
        }

        super.disable();
    }

    private void registerListeners(Listener[] listeners)
    {
        for (Listener l : listeners)
        {
            registerListener(l);
        }
    }

    private void registerListener(Listener l)
    {
        pluginManager.registerEvents(l, this);
    }

    /**
     * Can be removed in PluginBase 0.0.30
     */
    @Override
    public boolean acceptSchedules()
    {
        return true;
    }

    @Override
    public void startMainLoop(Runnable r)
    {
        // workaround: 如果插件在 enable() 中选择禁用自己，XiaMoJavaPlugin 仍会选择继续 startMainLoop()
        //             所以我们需要检查插件是否被启动
        if (!this.isEnabled()) return;

        Bukkit.getGlobalRegionScheduler().runAtFixedRate(this, o -> r.run(), 1, 1);
    }

    @Override
    public void runAsync(Runnable r)
    {
        Bukkit.getAsyncScheduler().runNow(this, o -> r.run());
    }

    @Override
    protected int getExceptionLimit()
    {
        return 3;
    }
}
