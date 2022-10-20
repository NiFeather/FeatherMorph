package xiamomc.morph;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import xiamomc.morph.commands.MorphCommandHelper;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.events.*;
import xiamomc.morph.interfaces.IManagePlayerData;
import xiamomc.morph.interfaces.IManageRequests;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.MorphMessageStore;
import xiamomc.pluginbase.Command.CommandHelper;
import xiamomc.pluginbase.XiaMoJavaPlugin;
import xiamomc.pluginbase.messages.MessageStore;

public final class MorphPlugin extends XiaMoJavaPlugin
{
    public static String getMorphNameSpace()
    {
        return "morphplugin";
    }

    @Override
    public String getNameSpace()
    {
        return getMorphNameSpace();
    }

    private final CommandHelper<MorphPlugin> cmdHelper = new MorphCommandHelper();

    private MorphManager morphManager;

    private PluginManager pluginManager;

    @Override
    public void onEnable()
    {
        super.onEnable();

        pluginManager = Bukkit.getPluginManager();

        var playerTracker = new PlayerTracker();
        var pluginEventListener = new PluginEventListener();
        pluginEventListener.onPluginEnable(this::onPluginEnable);

        //缓存依赖
        dependencyManager.cache(this);
        dependencyManager.cache(morphManager = new MorphManager());
        dependencyManager.cache(cmdHelper);
        dependencyManager.cacheAs(MessageStore.class, new MorphMessageStore());
        dependencyManager.cacheAs(MiniMessage.class, MiniMessage.miniMessage());;
        dependencyManager.cacheAs(IManagePlayerData.class, morphManager);
        dependencyManager.cacheAs(IManageRequests.class, new RequestManager());
        dependencyManager.cacheAs(MorphConfigManager.class, new MorphConfigManager(this));
        dependencyManager.cache(playerTracker);

        dependencyManager.cache(new MessageUtils());

        //注册EventProcessor
        this.schedule(c ->
        {
            registerListeners(new Listener[]
                    {
                            playerTracker,
                            pluginEventListener,
                            new CommonEventProcessor(),
                            new ReverseControlProcessor(),
                    });

            if (pluginManager.getPlugin("GSit") != null)
                registerListener(new GSitCompactProcessor());
        });
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
        }
        catch (Exception e)
        {
            logger.warn("禁用时出现问题：" + e.getMessage());
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
        if (name.equals("GSit"))
            registerListener(new GSitCompactProcessor());
    }
}
