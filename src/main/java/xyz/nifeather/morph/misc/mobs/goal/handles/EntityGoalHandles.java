package xyz.nifeather.morph.misc.mobs.goal.handles;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import xyz.nifeather.morph.misc.mobs.goal.handles.impl.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityGoalHandles
{
    public EntityGoalHandles()
    {
        initHandles();
    }

    private final Map<EntityType, IEntityGoalHandle<?>> handleMap = new ConcurrentHashMap<>();

    private void initHandles()
    {
        register(EntityType.CAT, new CatHandle());
        register(EntityType.PANDA, new PandaHandle());
        register(EntityType.OCELOT, new OcelotHandle());
        register(EntityType.RABBIT, new RabbitHandle());
    }

    private void register(EntityType type, IEntityGoalHandle<?> handle)
    {
        handleMap.put(type, handle);
    }

    public final CommonMobHandle fallbackMobHandle = new CommonMobHandle();

    public void handle(Mob mob)
    {
        var handle = (IEntityGoalHandle<Mob>) handleMap.getOrDefault(mob.getType(), null);
        if (handle == null)
        {
            fallbackMobHandle.tryCast(mob).ifPresent(fallbackMobHandle::apply);
            return;
        }

        handle.tryCast(mob).ifPresent(handle::apply);
    }

    private static final EntityGoalHandles instance = new EntityGoalHandles();

    public static EntityGoalHandles instance()
    {
        return instance;
    }
}