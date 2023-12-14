package xiamomc.morph.backends.server.renderer.network.registries;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

import java.util.Map;
import java.util.function.BiConsumer;

public class RegistryParameters
{
    public EntityType bukkitType()
    {
        return get(EntryIndex.ENTITY_TYPE);
    }
    public String disguiseName()
    {
        return get(EntryIndex.CUSTOM_NAME);
    }
    public SingleWatcher watcher()
    {
        return get(EntryIndex.BINDING_WATCHER);
    }
    public GameProfile getProfile()
    {
        return get(EntryIndex.PROFILE);
    }
    public RegistryParameters setProfile(GameProfile value)
    {
        this.open().write(EntryIndex.PROFILE, value).close();
        return this;
    }

    // Name <-> Value
    private final Map<String, Object> objectMap = new Object2ObjectOpenHashMap<>();
    private final Map<String, Object> dirty = new Object2ObjectOpenHashMap<>();

    private BiConsumer<RegistryParameters, Map<String, Object>> onParametersChange;

    public void onParametersChange(BiConsumer<RegistryParameters, Map<String, Object>> consumer)
    {
        this.onParametersChange = consumer;
    }

    //region r/w

    private boolean writing;
    private boolean wroteOnce;
    private final boolean lockedDownCritical;

    @Nullable
    public <X> X get(RegistryKey<X> key)
    {
        var val = objectMap.getOrDefault(key.name, null);

        if (val == null) return null;

        if (key.type.isInstance(val))
        {
            return (X)val;
        }
        else
        {
            MorphPlugin.getInstance().getSLF4JLogger().warn("Find incompatible value '%s' for key '%s'!".formatted(val, key));

            return null;
        }
    }

    public boolean writing()
    {
        return writing;
    }

    public RegistryParameters open()
    {
        if (writing)
            throw new IllegalStateException("There is another instance opening for write!");

        writing = true;
        return this;
    }

    public <X> RegistryParameters write(RegistryKey<X> key, X val)
    {
        if (!writing)
            throw new IllegalStateException("Use open() before calling write()");

        if (key.requireNonNull() && val == null)
            throw new IllegalArgumentException("Null value for non-null key '%s'!".formatted(key.name));

        if (lockedDownCritical)
        {
            if (key.equals(EntryIndex.BINDING_WATCHER) || key.equals(EntryIndex.ENTITY_TYPE))
                throw new IllegalArgumentException("May not set '%s' after init".formatted(key.name));
        }

        wroteOnce = true;
        objectMap.put(key.name, val);
        dirty.put(key.name, val);
        return this;
    }

    public void close()
    {
        writing = false;

        if (wroteOnce && onParametersChange != null)
        {
            onParametersChange.accept(this, new Object2ObjectOpenHashMap<>(dirty));
            dirty.clear();
        }

        wroteOnce = false;
    }

    //endregion r/w

    public RegistryParameters(@NotNull EntityType bukkitType, @NotNull String customName,
                              @NotNull SingleWatcher singleWatcher,
                              GameProfile profile)
    {
        if (bukkitType == null)
            throw new NullDependencyException("Null Bukkit Type");

        this.open()
                .write(EntryIndex.PROFILE, profile)
                .write(EntryIndex.ENTITY_TYPE, bukkitType)
                .write(EntryIndex.CUSTOM_NAME, customName)
                .write(EntryIndex.BINDING_WATCHER, watcher())
                .close();

        lockedDownCritical = true;
    }

    public static RegistryParameters fromBukkitType(EntityType bukkitType, SingleWatcher watcher)
    {
        return new RegistryParameters(bukkitType, "Nil", watcher, null);
    }
}
