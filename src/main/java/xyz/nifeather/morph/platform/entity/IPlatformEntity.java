package xyz.nifeather.morph.platform.entity;

import xyz.nifeather.morph.platform.world.IPlatformLocation;
import xyz.nifeather.morph.platform.world.IPlatformWorld;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IPlatformEntity
{
    int entityId();

    UUID uuid();

    IPlatformLocation location();

    IPlatformWorld world();

    CompletableFuture<Boolean> teleportAsync(IPlatformLocation targetLocation);

    void damage(double amount);
}
