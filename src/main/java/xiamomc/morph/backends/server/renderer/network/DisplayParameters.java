package xiamomc.morph.backends.server.renderer.network;

import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;

public record DisplayParameters(@Nullable GameProfile gameProfile, @NotNull String playerDisguiseName,
                                org.bukkit.entity.EntityType bukkitType,
                                @NotNull SingleWatcher watcher)
{
    public static DisplayParameters from(org.bukkit.entity.EntityType bukkitType, SingleWatcher watcher)
    {
        return new DisplayParameters(null, "Nil", bukkitType, watcher);
    }

    public static DisplayParameters fromRegistry(RegistryParameters registryParameters)
    {
        return new DisplayParameters(null, registryParameters.customName(), registryParameters.bukkitType(), registryParameters.singleWatcher());
    }
}
