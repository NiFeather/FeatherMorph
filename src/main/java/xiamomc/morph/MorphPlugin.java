package xiamomc.morph;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.LivingWatcher;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import xiamomc.morph.commands.MorphCommandHelper;
import xiamomc.morph.events.EventProcessor;
import xiamomc.pluginbase.Command.CommandHelper;
import xiamomc.pluginbase.XiaMoJavaPlugin;

public final class MorphPlugin extends XiaMoJavaPlugin {

    private static MorphPlugin instance;

    public static MorphPlugin GetInstance()
    {
        return instance;
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
        dependencyManager.Cache(new MorphUtils());

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
