package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.AbstractValues;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.morph.backends.server.renderer.utilties.WatcherUtils;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class SingleWatcher extends MorphPluginObject
{
    protected void initRegistry()
    {
    }

    protected void initValues()
    {
        for (SingleValue<?> singleValue : registry.keySet())
        {
            var newValue = singleValue.defaultValue();
            var randoms = singleValue.getRandomValues();
            if (!randoms.isEmpty())
            {
                var index = new Random().nextInt(randoms.size());
                newValue = randoms.get(index);

                this.write(singleValue.index(), newValue);
            }
        }
    }

    public final UUID bindingUUID;

    private Player bindingPlayer;

    public boolean isActive()
    {
        // 以目前的框架来看，似乎只能这样了 :(
        if (bindingPlayer.isOnline())
        {
            return true;
        }
        else
        {
            var player = Bukkit.getPlayer(bindingUUID);
            return player != null;
        }
    }

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
        initRegistry();
        initValues();
    }

    private final AtomicBoolean syncedOnce = new AtomicBoolean(false);

    @Initializer
    private void load()
    {
        if (!syncedOnce.get() && !disposed)
            sync();
    }

    //region Custom Registry

    protected final Map<String, Object> customRegistry = new Object2ObjectOpenHashMap<>();

    public <X> void write(RegistryKey<X> key, X value)
    {
        var prev = getOrDefault(key, null);
        customRegistry.put(key.name, value);

        onCustomWrite(key, prev, value);
    }

    protected <X> void onCustomWrite(RegistryKey<X> key, @Nullable X oldVal, @Nullable X newVal)
    {
    }

    public <X> X getOrDefault(RegistryKey<X> key, X defaultValue)
    {
        var val = get(key);

        return val == null ? defaultValue : val;
    }

    @Nullable
    public <X> X get(RegistryKey<X> key)
    {
        var val = customRegistry.getOrDefault(key.name, null);

        if (val == null) return null;

        if (key.type.isInstance(val))
        {
            return (X)val;
        }
        else
        {
            logger.warn("Find incompatible value '%s' for key '%s'!".formatted(val, key));

            return null;
        }
    }

    //endregion Custom Registry

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
            allSuccess = register(value) && allSuccess;

        return allSuccess;
    }

    protected boolean register(SingleValue<?> singleValue)
    {
        if (registry.keySet().stream().anyMatch(sv -> sv.index() == singleValue.index())) return false;

        registry.put(singleValue, singleValue.defaultValue());
        return true;
    }

    public <X> void write(SingleValue<X> singleValue, X value)
    {
        var prev = (X) registry.getOrDefault(singleValue, null);
        registry.put(singleValue, value);

        if (!value.equals(prev))
            dirtySingles.put(singleValue, value);

        onTrackerWrite(singleValue, prev, value);

        if (!syncing)
            sendPacketToAffectedPlayers(packetFactory.buildDiffMetaPacket(getBindingPlayer(), this));
    }

    public void write(int index, Object value)
    {
        var single = getSingle(index);
        if (single == null)
            throw new NullDependencyException("No registry found for index '%s'".formatted(index));

        if (!single.defaultValue().getClass().isInstance(value))
            throw new IllegalArgumentException("Incompatable value for index '%s', excepted for '%s', but got '%s'".formatted(index, single.defaultValue().getClass(), value.getClass()));

        write((SingleValue<Object>)single, value);
    }

    @Resolved(shouldSolveImmediately = true)
    private PacketFactory packetFactory;

    protected PacketFactory getPacketFactory()
    {
        return packetFactory;
    }

    protected <X> void onTrackerWrite(SingleValue<X> single, @Nullable X oldVal, @Nullable X newVal)
    {
    }

    public <X> X get(SingleValue<X> singleValue)
    {
        return (X)registry.getOrDefault(singleValue, singleValue.defaultValue());
    }

    public Object get(int index)
    {
        var single = getSingle(index);
        if (single == null)
            throw new NullDependencyException("No registry found for index '%s'".formatted(index));

        return get(single);
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

    private boolean syncing;

    public void sync()
    {
        syncing = true;

        syncedOnce.set(true);
        dirtySingles.clear();

        try
        {
            doSync();
        }
        catch (Throwable t)
        {
            logger.warn("Error occurred while syncing watcher: " + t.getMessage());
            t.printStackTrace();
        }

        syncing = false;
    }

    protected void doSync()
    {
    }

    public void mergeFromCompound(CompoundTag nbt)
    {
    }

    public void writeToCompound(CompoundTag nbt)
    {
    }

    //region Networking

    //endregion Networking

    protected List<Player> getAffectedPlayers(Player sourcePlayer)
    {
        return WatcherUtils.getAffectedPlayers(sourcePlayer);
    }

    protected void sendPacketToAffectedPlayers(PacketContainer packet)
    {
        var players = getAffectedPlayers(getBindingPlayer());

        var protocol = ProtocolLibrary.getProtocolManager();
        players.forEach(p -> protocol.sendServerPacket(p, packet));
    }

    private boolean disposed;

    public boolean disposed()
    {
        return disposed;
    }

    public final void dispose()
    {
        if (disposed)
            throw new RuntimeException("Already disposed!");

        disposed = true;

        try
        {
            onDispose();
        }
        catch (Throwable t)
        {
            logger.warn("Error occurred while disposing: " + t.getMessage());
            t.printStackTrace();
        }
    }

    protected void onDispose()
    {
    }
}
