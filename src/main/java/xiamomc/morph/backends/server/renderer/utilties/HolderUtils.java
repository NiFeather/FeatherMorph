package xiamomc.morph.backends.server.renderer.utilties;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;

public class HolderUtils
{
    private static ServerLevel level;

    private static void setupLevel()
    {
        if (level != null) return;

        level = ((CraftWorld)Bukkit.getWorlds().stream().findFirst().get()).getHandle();
    }

    private static <T> Registry<T> getRegistry(ResourceKey<Registry<T>> registryKey)
    {
        setupLevel();

        return level.registryAccess().registryOrThrow(registryKey);
    }

    public static <T> Holder<T> getHolderFor(ResourceKey<T> key, ResourceKey<Registry<T>> registryKey)
    {
        setupLevel();

        var registry = getRegistry(registryKey);

        var holderOptional = registry.getHolder(key);
        if (holderOptional.isEmpty())
            throw new NullPointerException("Holder for key '%s' does not found in the registry '%s'.".formatted(key, registry.key()));

        return holderOptional.get();
    }
}
