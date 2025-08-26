package xyz.nifeather.morph.platform.impl.paper.entity;

import org.bukkit.entity.LivingEntity;
import xyz.nifeather.morph.platform.CurrentPlatform;
import xyz.nifeather.morph.platform.entity.IPlatformEntity;
import xyz.nifeather.morph.platform.world.IPlatformLocation;
import xyz.nifeather.morph.platform.impl.paper.PaperPlatform;

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
    public CompletableFuture<Boolean> teleportAsync(IPlatformLocation targetLocation)
    {
        var nativeLocation = platform.worldLookup().getNativeLocation(targetLocation);
        return this.getHandle().teleportAsync(nativeLocation);
    }

    public LivingEntity getHandle()
    {
        return handle;
    }
}