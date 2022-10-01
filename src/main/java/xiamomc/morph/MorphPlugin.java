package xiamomc.morph;

import org.bukkit.Bukkit;
import xiamomc.morph.commands.MorphCommandHelper;
import xiamomc.morph.events.EventProcessor;
import xiamomc.pluginbase.Command.CommandHelper;
import xiamomc.pluginbase.XiaMoJavaPlugin;

public final class MorphPlugin extends XiaMoJavaPlugin {

    private static MorphPlugin instance;

    public static String getMorphNameSpace()
    {
        return "morph";
    }

    @Override
    public String getNameSpace() {
        return getMorphNameSpace();
    }

    public MorphPlugin()
    {
        if (instance != null) throw new RuntimeException("单实例插件");

        instance = this;
    }

    private CommandHelper cmdHelper = new MorphCommandHelper();

    @Override
    public void onEnable() {
        // Plugin startup logic
        super.onEnable();

        this.getSLF4JLogger().info("Enabling MorphPlugin");
        dependencyManager.Cache(this);
        dependencyManager.Cache(new MorphManager());

        this.schedule(c ->
        {
            Bukkit.getPluginManager().registerEvents(new EventProcessor(), this);
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        this.getSLF4JLogger().info("Disabling MorphPlugin");
        super.onDisable();
    }
}
