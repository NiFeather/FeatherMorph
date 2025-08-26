package xyz.nifeather.morph.platform.entity;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public interface IPlatformEntityLookup<TNativeEntity, TNativePlayer>
{
    @Nullable
    @Contract("null -> null; !null -> !null")
    public IPlatformPlayer getPlatformPlayer(@Nullable TNativePlayer nativePlayer);

    @Nullable
    @Contract("null -> null; !null -> !null")
    public TNativePlayer getNativePlayer(@Nullable IPlatformPlayer platformPlayer);

    @Nullable
    @Contract("null -> null; !null -> !null")
    public IPlatformEntity getPlatformEntity(@Nullable TNativeEntity nativeEntity);

    @Nullable
    @Contract("null -> null; !null -> !null")
    public TNativeEntity getNativeEntity(@Nullable IPlatformEntity platformEntity);
}
