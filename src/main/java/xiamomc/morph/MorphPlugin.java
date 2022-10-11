package xiamomc.morph;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import xiamomc.morph.commands.MorphCommandHelper;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.events.EventProcessor;
import xiamomc.morph.interfaces.IManagePlayerData;
import xiamomc.morph.interfaces.IManageRequests;
import xiamomc.morph.messages.MessageStore;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.pluginbase.Command.CommandHelper;
import xiamomc.pluginbase.XiaMoJavaPlugin;

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

    public MorphPlugin()
    {
    }

    private final CommandHelper<MorphPlugin> cmdHelper = new MorphCommandHelper();

    private MorphManager morphManager;

    @Override
    public void onEnable()
    {
        // Plugin startup logic
        super.onEnable();

        dependencyManager.Cache(this);
        dependencyManager.Cache(morphManager = new MorphManager());
        dependencyManager.Cache(cmdHelper);
        dependencyManager.Cache(new MessageStore());
        dependencyManager.CacheAs(MiniMessage.class, MiniMessage.miniMessage());;
        dependencyManager.CacheAs(IManagePlayerData.class, morphManager);
        dependencyManager.CacheAs(IManageRequests.class, new RequestManager());
        dependencyManager.CacheAs(MorphConfigManager.class, new MorphConfigManager(this));

        dependencyManager.Cache(new MessageUtils());

        this.schedule(c ->
        {
            Bukkit.getPluginManager().registerEvents(new EventProcessor(), this);
        });
    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic

        super.onDisable();

        if (morphManager != null)
            morphManager.onPluginDisable();
    }
}
