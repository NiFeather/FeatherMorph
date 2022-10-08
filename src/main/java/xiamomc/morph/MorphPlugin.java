package xiamomc.morph;

import org.bukkit.Bukkit;
import xiamomc.morph.commands.MorphCommandHelper;
import xiamomc.morph.events.EventProcessor;
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

        this.schedule(c ->
        {
            Bukkit.getPluginManager().registerEvents(new EventProcessor(), this);
        });
    }

    @Override
    public void onDisable()
    {
        // Plugin shutdown logic

        if (morphManager != null)
            morphManager.onPluginDisable();

        super.onDisable();
    }
}
