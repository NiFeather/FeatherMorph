package xiamomc.morph;

import io.papermc.paper.util.Tick;
import io.papermc.paper.util.TickThread;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;
import xiamomc.pluginbase.PluginObject;

public class MorphPluginObject extends PluginObject<MorphPlugin>
{
    @Override
    protected String getPluginNamespace()
    {
        return MorphPlugin.getMorphNameSpace();
    }

    /**
     * 在某一实体上运行任务
     */
    protected void scheduleOn(Entity entity, Runnable r)
    {
        this.scheduleOn(entity, r, 1);
    }

    /**
     * 在某一实体上运行任务
     *
     * @param delay 执行延迟，若为0则尽量使其立即执行
     */
    protected void scheduleOn(Entity entity, Runnable r, int delay)
    {
        if (delay == 0)
        {
            var nmsEntity = ((CraftEntity) entity).getHandle();
            if (TickThread.isTickThreadFor(nmsEntity))
            {
                r.run();
                return;
            }
            else
            {
                delay = 1;
            }
        }

        entity.getScheduler().execute(plugin, r, null, delay);
    }

    protected void scheduleWorld(Entity entity, Runnable r)
    {
        Bukkit.getRegionScheduler().execute(plugin, entity.getLocation(), r);
    }
}
