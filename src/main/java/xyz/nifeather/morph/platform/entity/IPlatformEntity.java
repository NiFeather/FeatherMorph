package xyz.nifeather.morph.platform.entity;

import xyz.nifeather.morph.platform.world.IPlatformLocation;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IPlatformEntity
{
    UUID uuid();

    IPlatformLocation location();

    CompletableFuture<Boolean> teleportAsync(IPlatformLocation targetLocation);
}
