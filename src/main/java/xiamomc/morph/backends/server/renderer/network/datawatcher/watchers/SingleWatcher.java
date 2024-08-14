package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.AbstractValues;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.morph.backends.server.renderer.network.registries.RenderRegistry;
import xiamomc.morph.backends.server.renderer.utilties.WatcherUtils;
import xiamomc.morph.misc.disguiseProperty.SingleProperty;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class SingleWatcher extends MorphPluginObject
{
    protected void initRegistry()
    {
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

    public boolean isPlayerOnline()
    {
        return Bukkit.getOfflinePlayer(bindingUUID).isOnline();
    }

    public Player getBindingPlayer()
    {
        if (!bindingPlayer.isConnected())
        {
            if (!Bukkit.getOfflinePlayer(bindingUUID).isOnline())
            {
                logger.warn("Calling getBindingPlayer for an offline player!");
                Thread.dumpStack();
            }
            else
            {
                bindingPlayer = Bukkit.getPlayer(bindingUUID);
            }
        }

        return bindingPlayer;
    }

    private final EntityType entityType;

    public EntityType getEntityType()
    {
        return entityType;
    }

    private boolean doingInitialization;

    public SingleWatcher(Player bindingPlayer, EntityType entityType)
    {
        this.bindingUUID = bindingPlayer.getUniqueId();
        this.bindingPlayer = bindingPlayer;

        this.entityType = entityType;

        doingInitialization = true;
        markSilent(this);

        initRegistry();
        doingInitialization = false;
        unmarkSilent(this);
    }

    private final AtomicBoolean syncedOnce = new AtomicBoolean(false);

    @Initializer
    private void load()
    {
        if (!syncedOnce.get() && !disposed)
            sync();
    }

    //region Disguise Property

    public final <X> void write(SingleProperty<X> property, X value)
    {
        this.onPropertyWrite(property, value);
    }

    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
    }

    //endregion Disguise Property

    //region Custom Registry

    protected final Map<String, Object> customRegistry = Collections.synchronizedMap(new Object2ObjectOpenHashMap<>());

    public <X> void writeEntry(RegistryKey<X> key, X value)
    {
        customRegistry.put(key.name, value);

        if (doingInitialization)
            return;

        var prev = getOrDefault(key, null);
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

    protected final Map<Integer, Object> overrides = new ConcurrentHashMap<>();

    private final List<SingleValue<?>> values = new ObjectArrayList<>();

    @Nullable
    public SingleValue<?> getSingle(int index)
    {
        return values.stream().filter(sv -> sv.index() == index)
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
        if (values.stream().anyMatch(sv -> sv.index() == singleValue.index())) return false;

        values.add(singleValue);
        return true;
    }

    public void remove(SingleValue<?> singleValue)
    {
        //commonRegistry.remove(singleValue.index());
        overrides.remove(singleValue.index());
    }

    /**
     * Write value to the temporary buffer (Dirty Singles) <br>
     * Mostly used in {@link SingleWatcher#doSync()} function.
     *
     * @apiNote If the given SingleValue has an override value, this operation will not be performed
     *          <br>
     *          If you wish to write a persistent value, use {@link SingleWatcher#writeOverride(SingleValue, Object)}
     */
    public <X> void writeTemp(SingleValue<X> singleValue, @NotNull X value)
    {
        if (this.overrides.containsKey(singleValue.index())) return;

        this.write(singleValue, value, true);
    }

    /**
     * Write value to override registry.
     * <br>
     * This action will be persistent until it get removed or a new value covers this
     */
    public <X> void writeOverride(SingleValue<X> singleValue, @NotNull X value)
    {
        this.write(singleValue, value, false);
    }

    private <X> void write(SingleValue<X> singleValue, @NotNull X value, boolean isTemp)
    {
        if (value == null)
            throw new IllegalArgumentException("If you wish to remove a SingleValue, use remove()");

        var prevOption = overrides.getOrDefault(singleValue.index(), null);
        var prev = prevOption == null ? null : (X)prevOption;

        if (!isTemp)
            overrides.put(singleValue.index(), value);

        if (!this.values.contains(singleValue))
            throw new IllegalArgumentException("Trying to write a value that does not belongs to this watcher");

        if (doingInitialization)
            return;

        if (!value.equals(prev))
            dirtySingles.put(singleValue, value);

        onTrackerWrite(singleValue, prev, value);

        if (!isSilent() && isAlive())
            sendPacketToAffectedPlayers(packetFactory.buildDiffMetaPacket(getBindingPlayer(), this));
    }

    public void write(int index, Object value, boolean isTemp)
    {
        var single = getSingle(index);
        if (single == null)
            throw new NullDependencyException("No registry found for index '%s'".formatted(index));

        if (!single.defaultValue().getClass().isInstance(value))
            throw new IllegalArgumentException("Incompatable value for index '%s', excepted for '%s', but got '%s'".formatted(index, single.defaultValue().getClass(), value.getClass()));

        write((SingleValue<Object>)single, value, isTemp);
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
        return this.getOr(singleValue, singleValue.defaultValue());
    }

    public Object get(int index)
    {
        var single = getSingle(index);
        if (single == null)
            throw new NullDependencyException("No registry found for index '%s'".formatted(index));

        return get(single);
    }

    public Object getOr(int index, Object defaultVal)
    {
        var single = getSingle(index);
        if (single == null)
            throw new NullDependencyException("No registry found for index '%s'".formatted(index));

        return getOr((SingleValue<Object>) single, defaultVal);
    }

    public <X> X getOr(SingleValue<X> singleValue, X defaultVal)
    {
        var option = this.overrides.getOrDefault(singleValue.index(), null);
        if (option == null) return defaultVal;
        else return (X) option;
    }

    /**
     * Gets the override values for this watcher
     * @apiNote This doesn't include values in the common registry!
     */
    public Map<Integer, Object> getOverrides()
    {
        return new Object2ObjectOpenHashMap<>(this.overrides);
    }

    /**
     * Gets the combined map for the common and overrided values
     */
    public Map<Integer, Object> getOverlayedRegistry()
    {
        var map = this.getOverrides();
        this.getDirty().forEach((sv, option) -> map.put(sv.index(), option));

        return map;
    }

    private final Map<SingleValue<?>, Object> dirtySingles = Collections.synchronizedMap(new Object2ObjectOpenHashMap<>());

    public Map<SingleValue<?>, Object> getDirty()
    {
        var dirty = new Object2ObjectOpenHashMap<>(dirtySingles);
        dirtySingles.clear();

        return dirty;
    }

    //endregion Value Registry

    private static final Object syncSilentSource = new Object();

    public void sync()
    {
        markSilent(syncSilentSource);

        syncedOnce.set(true);
        dirtySingles.clear();

        try
        {
            /*
            if (!isPlayerOnline())
                throw new IllegalStateException("Can't sync value for offline player!");

            var nmsPlayer = NmsRecord.ofPlayer(getBindingPlayer());
            TickThread.ensureTickThread(nmsPlayer, "Syncing watcher's value while not on its player's ticking thread!");
            */

            doSync();
        }
        catch (Throwable t)
        {
            logger.warn("Error occurred while syncing watcher: " + t.getMessage());
            t.printStackTrace();
        }

        unmarkSilent(syncSilentSource);
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

    // 针对构建生成包之前就有customWrite的缓解方案: RenderRegistry#register(Player player, RegisterParameters registerParameters)
    // 或许需要找一种办法能让SingleWatcher在初始化值的时候不要发送任何数据包
    private final Collection<Object> silentRequestSources = new ObjectArrayList<>();

    public void markSilent(Object source)
    {
        silentRequestSources.add(source);
    }

    public void unmarkSilent(Object source)
    {
        silentRequestSources.remove(source);
    }

    public boolean isSilent()
    {
        return !silentRequestSources.isEmpty();
    }

    private final AtomicReference<RenderRegistry> parentRegistryRef = new AtomicReference<>();

    public void setParentRegistry(RenderRegistry renderRegistry)
    {
        this.parentRegistryRef.set(renderRegistry);
    }

    public boolean isAlive()
    {
        return parentRegistryRef.get() != null;
    }

    //endregion Networking

    protected List<Player> getAffectedPlayers(Player sourcePlayer)
    {
        return WatcherUtils.getAffectedPlayers(sourcePlayer);
    }

    protected void sendPacketToAffectedPlayers(PacketContainer packet)
    {
        if (isSilent())
        {
            logger.warn("Not sending packets: Sending packets while we should be silent?!");
            Thread.dumpStack();
            return;
        }

        if (!isAlive())
        {
            logger.warn("Not sending packets: Sending packets while the watcher isn't alive!");
            Thread.dumpStack();
            return;
        }

        var players = getAffectedPlayers(getBindingPlayer());

        var protocol = ProtocolLibrary.getProtocolManager();
        players.forEach(p -> protocol.sendServerPacket(p, packet));
    }

    private boolean disposed;

    public boolean disposed()
    {
        return disposed;
    }

    @Override
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
