package xyz.nifeather.morph.platform.impl.paper.entity;

import org.bukkit.entity.LivingEntity;
import xyz.nifeather.morph.platform.CurrentPlatform;
import xyz.nifeather.morph.platform.entity.IPlatformEntity;
import xyz.nifeather.morph.platform.world.IPlatformLocation;
import xyz.nifeather.morph.platform.impl.paper.PaperPlatform;
import xyz.nifeather.morph.platform.world.IPlatformWorld;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PaperEntity implements IPlatformEntity
{
    private final LivingEntity handle;
    private final PaperPlatform platform;

    public PaperEntity(LivingEntity handle)
    {
        this.handle = handle;
        this.platform = CurrentPlatform.instance();
    }

    @Override
    public int entityId()
    {
        return handle.getEntityId();
    }

    @Override
    public UUID uuid()
    {
        return handle.getUniqueId();
    }

    @Override
    public IPlatformLocation location()
    {
        return platform.worldLookup().getPlatformLocation(handle.getLocation());
    }

    @Override
    public IPlatformWorld world()
    {
        return platform.worldLookup().getPlatformWorld(handle.getWorld());
    }

    @Override
    public CompletableFuture<Boolean> teleportAsync(IPlatformLocation targetLocation)
    {
        var nativeLocation = platform.worldLookup().getNativeLocation(targetLocation);
        return this.getHandle().teleportAsync(nativeLocation);
    }

    @Override
    public void damage(double amount)
    {
        handle.damage(amount);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof PaperEntity other)) return false;

        return handle.equals(other.handle);
    }

    public LivingEntity getHandle()
    {
        return handle;
    }
}