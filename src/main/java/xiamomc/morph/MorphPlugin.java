package xiamomc.morph;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.ApiStatus;
import xiamomc.morph.abilities.AbilityHandler;
import xiamomc.morph.commands.MorphCommandManager;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.events.CommonEventProcessor;
import xiamomc.morph.events.InteractionMirrorProcessor;
import xiamomc.morph.events.PlayerTracker;
import xiamomc.morph.events.PluginEventListener;
import xiamomc.morph.interfaces.IManagePlayerData;
import xiamomc.morph.interfaces.IManageRequests;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphMessageStore;
import xiamomc.morph.messages.vanilla.VanillaMessageStore;
import xiamomc.morph.misc.PlayerOperationSimulator;
import xiamomc.morph.misc.integrations.gsit.GSitCompactProcessor;
import xiamomc.morph.misc.integrations.placeholderapi.PlaceholderIntegration;
import xiamomc.morph.network.server.MorphClientHandler;
import xiamomc.morph.skills.MorphSkillHandler;
import xiamomc.morph.storage.skill.SkillAbilityConfigurationStore;
import xiamomc.pluginbase.Command.CommandHelper;
import xiamomc.pluginbase.Messages.MessageStore;
import xiamomc.pluginbase.XiaMoJavaPlugin;

public final class MorphPlugin extends XiaMoJavaPlugin
{
    private static MorphPlugin instance;

    /**
     * 仅当当前对象无法继承MorphPluginObject或不需要完全继承MorphPluginObject时使用
     * @return 插件的实例
     */
    @Deprecated
    public static MorphPlugin getInstance()
    {
        return instance;
    }

    public MorphPlugin()
    {
        instance = this;
    }

    public static String getMorphNameSpace()
    {
        return "morphplugin";
    }

    @Override
    public String getNameSpace()
    {
        return getMorphNameSpace();
    }

    private final CommandHelper<MorphPlugin> cmdHelper = new MorphCommandManager();

    private MorphManager morphManager;

    private PluginManager pluginManager;

    private final MorphSkillHandler skillHandler = new MorphSkillHandler();

    private final AbilityHandler abilityHandler = new AbilityHandler();

    private final VanillaMessageStore vanillaMessageStore = new VanillaMessageStore();

    private PlaceholderIntegration placeholderIntegration;

    private MorphClientHandler clientHandler;

    private Metrics metrics;

    private InteractionMirrorProcessor mirrorProcessor;

    @Override
    public void onEnable()
    {
        super.onEnable();

        this.metrics = new Metrics(this, 18062);

        pluginManager = Bukkit.getPluginManager();

        clientHandler = new MorphClientHandler();

        var playerTracker = new PlayerTracker();
        var pluginEventListener = new PluginEventListener();

        pluginEventListener.onPluginEnable(this::onPluginEnable);

        //缓存依赖
        dependencyManager.cache(this);
        dependencyManager.cache(morphManager = new MorphManager());
        dependencyManager.cache(skillHandler);
        dependencyManager.cache(abilityHandler);
        dependencyManager.cache(cmdHelper);
        dependencyManager.cache(clientHandler);
        dependencyManager.cache(vanillaMessageStore);

        dependencyManager.cacheAs(MessageStore.class, new MorphMessageStore());
        dependencyManager.cacheAs(MiniMessage.class, MiniMessage.miniMessage());
        dependencyManager.cacheAs(IManagePlayerData.class, morphManager);
        dependencyManager.cacheAs(IManageRequests.class, new RequestManager());
        dependencyManager.cacheAs(Scoreboard.class, Bukkit.getScoreboardManager().getMainScoreboard());
        dependencyManager.cacheAs(MorphConfigManager.class, new MorphConfigManager(this));
        dependencyManager.cache(playerTracker);

        dependencyManager.cache(new SkillAbilityConfigurationStore());

        dependencyManager.cache(new MessageUtils());

        dependencyManager.cache(new PlayerOperationSimulator());

        mirrorProcessor = new InteractionMirrorProcessor();

        //注册EventProcessor
        this.schedule(() ->
        {
            registerListeners(new Listener[]
                    {
                            playerTracker,
                            pluginEventListener,
                            mirrorProcessor,
                            new CommonEventProcessor(),
                    });

            for (Plugin plugin : pluginManager.getPlugins())
                onPluginEnable(plugin.getName());

            clientHandler.sendReAuth(Bukkit.getOnlinePlayers());
        });
    }

    @ApiStatus.Internal
    public void crash(Throwable t)
    {
        logger.error(t.getLocalizedMessage());
        t.printStackTrace();

        this.onDisable();
    }

    @Override
    public void onDisable()
    {
        //调用super.onDisable后依赖管理器会被清空
        //需要在调用前先把一些东西处理好
        try
        {
            if (morphManager != null)
                morphManager.onPluginDisable();

            if (placeholderIntegration != null)
                placeholderIntegration.unregister();

            if (clientHandler != null)
                clientHandler.getConnectedPlayers().forEach(clientHandler::disconnect);

            if (metrics != null)
                metrics.shutdown();

            if (mirrorProcessor != null)
                mirrorProcessor.pushToLoggingBase();
        }
        catch (Exception e)
        {
            logger.warn("Error occurred while disabling: " + e.getMessage());
            e.printStackTrace();
        }

        super.onDisable();
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

    public void onPluginEnable(String name)
    {
        switch (name)
        {
            case "GSit" ->
            {
                registerListener(new GSitCompactProcessor());
            }

            case "PlaceholderAPI" ->
            {
                placeholderIntegration = new PlaceholderIntegration(dependencyManager);
                placeholderIntegration.register();
            }
        }
    }
}
