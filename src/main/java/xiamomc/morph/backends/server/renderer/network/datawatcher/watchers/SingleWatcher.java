package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.AbstractValues;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

import java.util.Map;
import java.util.UUID;

public abstract class SingleWatcher extends MorphPluginObject
{
    public final UUID bindingUUID;

    private Player bindingPlayer;

    public Player getBindingPlayer()
    {
        if (!bindingPlayer.isOnline())
        {
            var player = Bukkit.getPlayer(bindingUUID);
            if (player == null)
            {
                logger.warn("Calling getBindingPlayer for an offline player!");
                Thread.dumpStack();
            }
            else
                bindingPlayer = player;
        }

        return bindingPlayer;
    }

    private final EntityType entityType;

    public EntityType getEntityType()
    {
        return entityType;
    }

    public SingleWatcher(Player bindingPlayer, EntityType entityType)
    {
        this.bindingUUID = bindingPlayer.getUniqueId();
        this.bindingPlayer = bindingPlayer;

        this.entityType = entityType;
    }

    //region Value Registry

    protected final Map<SingleValue<?>, Object> registry = new Object2ObjectOpenHashMap<>();

    @Nullable
    public SingleValue<?> getSingle(int index)
    {
        return registry.keySet().stream().filter(sv -> sv.index() == index)
                .findFirst().orElse(null);
    }

    protected boolean register(AbstractValues values)
    {
        var allSuccess = true;
        for (SingleValue<?> value : values.getValues())
            allSuccess = allSuccess && register(value);

        return allSuccess;
    }

    protected boolean register(SingleValue<?> singleValue)
    {
        if (registry.containsKey(singleValue)) return false;

        registry.put(singleValue, singleValue.defaultValue());
        return true;
    }

    public <X> void write(SingleValue<X> singleValue, X value)
    {
        write(singleValue.index(), value);
    }

    public void write(int index, Object value)
    {
        var single = getSingle(index);
        if (single == null)
            throw new NullDependencyException("No registry found for index '%s'".formatted(index));

        if (!single.defaultValue().getClass().isInstance(value))
            throw new IllegalArgumentException("Incompatable value for index '%s', excepted for '%s', but got '%s'".formatted(index, single.defaultValue().getClass(), value.getClass()));

        registry.put(single, value);
        dirtySingles.put(single, value);
    }

    public <X> X get(SingleValue<X> singleValue)
    {
        var ret = get(singleValue.index());
        return (X)ret;
    }

    public Object get(int index)
    {
        var single = getSingle(index);
        if (single == null)
            throw new NullDependencyException("No registry found for index '%s'".formatted(index));

        return registry.get(single);
    }

    public Map<SingleValue<?>, Object> getRegistry()
    {
        return new Object2ObjectOpenHashMap<>(registry);
    }

    private final Map<SingleValue<?>, Object> dirtySingles = new Object2ObjectOpenHashMap<>();

    public Map<SingleValue<?>, Object> getDirty()
    {
        var dirty = new Object2ObjectOpenHashMap<>(dirtySingles);
        dirtySingles.clear();

        return dirty;
    }

    //endregion Value Registry

    public void sync()
    {
        dirtySingles.clear();
        doSync();
    }

    protected void doSync()
    {
    }

    //region Networking

    //endregion Networking
}
