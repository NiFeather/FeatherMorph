package xyz.nifeather.morph.platform.world;

import java.util.UUID;

public interface IPlatformWorldLookup<TNativeWorld, TNativeLocation>
{
    IPlatformWorld lookup(String name);
    IPlatformWorld lookup(UUID uuid);

    IPlatformWorld getPlatformWorld(TNativeWorld nativeWorld);
    TNativeWorld getNativeWorld(IPlatformWorld platformWorld);

    IPlatformLocation getPlatformLocation(TNativeLocation nativeLocation);
    TNativeLocation getNativeLocation(IPlatformLocation platformLocation);
}
