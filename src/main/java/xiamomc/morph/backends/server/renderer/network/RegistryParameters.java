package xiamomc.morph.backends.server.renderer.network;

import org.jetbrains.annotations.NotNull;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

public record RegistryParameters(@NotNull org.bukkit.entity.EntityType bukkitType, String customName, @NotNull SingleWatcher singleWatcher)
{
    public RegistryParameters(@NotNull org.bukkit.entity.EntityType bukkitType, String customName, @NotNull SingleWatcher singleWatcher)
    {
        this.bukkitType = bukkitType;
        this.customName = customName;
        this.singleWatcher = singleWatcher;

        if (bukkitType == null)
            throw new NullDependencyException("Null Bukkit Type");
    }

    public static RegistryParameters fromBukkitType(org.bukkit.entity.EntityType bukkitType, SingleWatcher watcher)
    {
        return new RegistryParameters(bukkitType, "Nil", watcher);
    }
}
