package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import xiamomc.pluginbase.Annotations.Initializer;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing.IBindTarget;
import xyz.nifeather.morph.utilities.FoliaThreadUtils;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BindableVirtualEntity<E extends LivingEntity> extends VirtualEntity
{
    protected final IBindTarget bindTarget;

    public BindableVirtualEntity(IBindTarget bindTarget, EntityType entityType)
    {
        super(entityType);
        this.bindTarget = bindTarget;
    }

    private final AtomicBoolean syncedOnce = new AtomicBoolean(false);

    public static final Object syncSilentSource = new Object();

    @Initializer
    private void load()
    {
        if (!syncedOnce.get() && !disposed)
            sync();
    }

    public void sync()
    {
        markSilent(syncSilentSource);

        syncedOnce.set(true);
        dirtyValues.clear();

        try
        {
            bindTarget.runSynchronously(t ->
            {
                doSync();
                return 0;
            }, Duration.ofSeconds(1));
        }
        catch (Throwable t)
        {
            logger.warn("Error occurred while syncing watcher", t);
        }

        unmarkSilent(syncSilentSource);
    }

    protected void doSync()
    {
    }
}
