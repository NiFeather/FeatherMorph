package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.backends.server.renderer.network.PacketFactory;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.AbstractValues;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.SingleValue;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;
import xyz.nifeather.morph.backends.server.renderer.utilties.WatcherUtils;
import xyz.nifeather.morph.misc.BuildFailedException;
import xyz.nifeather.morph.misc.ExecutionErrorException;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.OffTreeProperties;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
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

    /**
     * Currently disguise properties are handled by the wrapper.
     * So the watcher only supports writing values so that the watcher could sync them with the wrapper.
     */
    public final <X> void writeProperty(SingleProperty<X> property, X value)
    {
        this.handlePropertyWriteInternal(property, value);
        this.onPropertyWrite(property, value);
    }

    // I think we should handle this in a more proper way...
    private <X> void handlePropertyWriteInternal(SingleProperty<X> property, X value)
    {
        if (OffTreeProperties.VIRTUAL_ENTITY_UUID.equals(property))
        {
            var uuid = (UUID) value;
            this.writeEntry(CustomEntries.SPAWN_UUID, uuid);
        }
    }

    protected <X> void onPropertyWrite(SingleProperty<X> property, X value)
    {
    }

    //endregion Disguise Property

    //region Custom Registry

    protected final Map<String, Object> customRegistry = Collections.synchronizedMap(new Object2ObjectOpenHashMap<>());

    public <X> void writeEntry(CustomEntry<X> entry, X value)
    {
        customRegistry.put(entry.name, value);

        if (doingInitialization)
            return;

        var prev = readEntryOrDefault(entry, null);
        onEntryWrite(entry, prev, value);
    }

    protected <X> void onEntryWrite(CustomEntry<X> entry, @Nullable X oldVal, @Nullable X newVal)
    {
    }

    @NotNull
    public <X> X readEntryOrThrow(CustomEntry<X> entry)
    {
        var val = this.readEntry(entry);
        if (val == null)
            throw new NullDependencyException("Custom entry '%s' not found in '%s'".formatted(entry, this.getClass().getSimpleName()));

        return val;
    }

    public <X> X readEntryOrDefault(CustomEntry<X> entry, X defaultValue)
    {
        var val = readEntry(entry);

        return val == null ? defaultValue : val;
    }

    @Nullable
    public <X> X readEntry(CustomEntry<X> entry)
    {
        var val = customRegistry.getOrDefault(entry.name, null);

        if (val == null) return null;

        if (entry.type.isInstance(val))
        {
            return (X)val;
        }
        else
        {
            logger.warn("Find incompatible value '%s' for custom entry '%s'!".formatted(val, entry));
            return null;
        }
    }

    //endregion Custom Registry

    public void resetRegistries()
    {
        Map<Integer, Object> registryCopy = new Object2ObjectOpenHashMap<>(registry);

        registryCopy.forEach((id, val) ->
        {
            var sv = this.knownValues.getOrDefault(id, null);
            if (sv != null)
                this.writePersistent((SingleValue<Object>) sv, sv.defaultValue());

            this.registry.remove(id);
        });

        Map<String, Object> crCopy = new Object2ObjectOpenHashMap<>(customRegistry);
        crCopy.clear();
    }

    //region Value Registry

    protected final Map<Integer, Object> registry = new ConcurrentHashMap<>();
    private final Map<Integer, SingleValue<?>> knownValues = new ConcurrentHashMap<>();

    public Map<Integer, SingleValue<?>> getKnownValues()
    {
        return new Object2ObjectOpenHashMap<>(knownValues);
    }

    /**
     * Values in this list shouldn't be included with meta packet processing in {@link SingleWatcher#handleEntityMetadataPacket(WrapperPlayServerEntityMetadata)}
     */
    private final List<Integer> blockedValues = Collections.synchronizedList(new ObjectArrayList<>());

    /**
     * Block specific value type (by index) from further updating.
     * <p>
     * Also prevents these values from appearing in the server's metadata packet.
     */
    public void block(int index)
    {
        if (!blockedValues.contains(index))
            blockedValues.add(index);
    }

    /**
     * Block specific value type from further updating
     * <p>
     * Also prevents these values from appearing in the server's metadata packet.
     */
    public void block(SingleValue<?> sv)
    {
        this.block(sv.index());
    }

    public void unBlock(int index)
    {
        blockedValues.remove((Integer) index);
    }

    public void unBlock(SingleValue<?> sv)
    {
        this.unBlock(sv.index());
    }

    /**
     * Get values(In index) to filter out from server's metadata packet
     */
    public List<Integer> getBlockedValues()
    {
        return new ObjectArrayList<>(blockedValues);
    }

    @Nullable
    public SingleValue<?> getSingle(int index)
    {
        return knownValues.getOrDefault(index, null);
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
        if (knownValues.containsKey(singleValue.index())) return false;

        knownValues.put(singleValue.index(), singleValue);
        return true;
    }

    public void remove(SingleValue<?> singleValue)
    {
        //commonRegistry.remove(singleValue.index());
        registry.remove(singleValue.index());
    }

    /**
     * Write value to the temporary buffer (Dirty Singles) <br>
     * Mostly used in {@link SingleWatcher#doSync()} function.
     *
     * @apiNote If the given SingleValue has an override value, this operation will not be performed
     *          <br>
     *          If you wish to write a persistent value, use {@link SingleWatcher#writePersistent(SingleValue, Object)}
     */
    public <X> void writeTemp(SingleValue<X> singleValue, @NotNull X value)
    {
        if (this.registry.containsKey(singleValue.index())) return;

        this.write(singleValue, value, false);
    }

    /**
     * Write value to override registry.
     * <br>
     * This action will be persistent until it get removed or a new value covers this
     */
    public <X> void writePersistent(SingleValue<X> singleValue, @NotNull X value)
    {
        this.write(singleValue, value, true);
    }

    /**
     * @return NULL if the external SV doesn't have a matching SV in this watcher
     * @param <X>
     */
    @Nullable
    public <X> SingleValue<X> tryCast(SingleValue<X> external)
    {
        var match = this.knownValues.getOrDefault(external.index(), null);
        if (match == null) return null;

        if (match.equals(external))
            return (SingleValue<X>) match;
        else
            return null;
    }

    private <X> void write(SingleValue<X> singleValue, @NotNull X value, boolean isPersistent)
    {
        if (value == null)
            throw new IllegalArgumentException("If you wish to remove a SingleValue, use remove()");

        if (!this.knownValues.containsValue(singleValue))
        {
            var cast = this.tryCast(singleValue);
            String message = "Trying to write a SV that doesn't belongs to this Watcher: '%s'. ".formatted(singleValue);

            if (cast == null)
                throw new IllegalArgumentException(message);
            else
                logger.warn(message + "You may want to use 'tryCast(...)' or 'getKnownValues()' to get the correct SV.");
        }

        var prevOption = registry.getOrDefault(singleValue.index(), null);
        var prev = prevOption == null ? null : (X)prevOption;

        if (isPersistent)
            registry.put(singleValue.index(), value);

        if (doingInitialization)
            return;

        if (!value.equals(prev))
            dirtyValues.put(singleValue, value);

        onTrackerWrite(singleValue, prev, value);

        if (!isSilent() && isAlive())
            sendPacketToAffectedPlayers(PacketFactory.buildDiffMetaPacket(this));
    }

    protected <X> void onTrackerWrite(SingleValue<X> single, @Nullable X oldVal, @Nullable X newVal)
    {
    }

    /**
     * @return A value set for this watcher, RETURNS DEFAULT VALUE IF NOT SET
     */
    @NotNull
    public <X> X read(SingleValue<X> singleValue)
    {
        return this.readOr(singleValue, singleValue.defaultValue());
    }

    public Object read(int index)
    {
        var single = getSingle(index);
        if (single == null)
            throw new NullDependencyException("No registry found for index '%s'".formatted(index));

        return read(single);
    }

    public Object readOr(int index, Object defaultVal)
    {
        var single = getSingle(index);
        if (single == null)
            throw new NullDependencyException("No registry found for index '%s'".formatted(index));

        return readOr((SingleValue<Object>) single, defaultVal);
    }

    public <X> X readOr(SingleValue<X> singleValue, X defaultVal)
    {
        var option = this.registry.getOrDefault(singleValue.index(), null);
        if (option == null) return defaultVal;
        else return (X) option;
    }

    public boolean isValuePresent(SingleValue<?> singleValue)
    {
        return isValuePresent(singleValue.index());
    }

    public boolean isValuePresent(int index)
    {
        return this.registry.containsKey(index);
    }

    /**
     * Gets the override values for this watcher
     * @apiNote This doesn't include values in the common registry!
     */
    public Map<Integer, Object> getRegistry()
    {
        return new Object2ObjectOpenHashMap<>(this.registry);
    }

    /**
     * Gets the combined map for the common and overrided values
     */
    public Map<Integer, Object> getOverlayedRegistry()
    {
        var map = this.getRegistry();
        this.getDirty().forEach((sv, option) -> map.putIfAbsent(sv.index(), option));

        return map;
    }

    private final Map<SingleValue<?>, Object> dirtyValues = Collections.synchronizedMap(new Object2ObjectOpenHashMap<>());

    public Map<SingleValue<?>, Object> getDirty()
    {
        return new Object2ObjectOpenHashMap<>(dirtyValues);
    }

    public void clearDirty()
    {
        dirtyValues.clear();
    }

    //endregion Value Registry

    private static final Object syncSilentSource = new Object();

    // Let's just keep this for a while...
    public void update()
    {
    }

    public final void handleEntityMetadataPacket(WrapperPlayServerEntityMetadata packetWrapper)
            throws ExecutionErrorException
    {
        //获取原Meta包中的数据
        var originalData = packetWrapper.getEntityMetadata();

        // 如果Meta包里有咱的标记，那么移除标记并返回原包
        if (originalData.removeIf(wrapped -> wrapped.getValue().equals(PacketFactory.MARK_DONT_PROCESS)))
        {
            packetWrapper.setEntityMetadata(originalData);
            return;
        }

        var overrideList = rebuildMetadata(originalData);

        // Then we ask the implementation if there's anything to modify
        overrideList = this.handleEntityMetadata(ImmutableList.copyOf(originalData), overrideList);
        packetWrapper.setEntityMetadata(new ArrayList<>(overrideList));
    }

    private List<EntityData<?>> rebuildMetadata(List<EntityData<?>> originalData) throws ExecutionErrorException
    {
        // First we do internal process
        List<EntityData<?>> overrideList = new ObjectArrayList<>();

        for (EntityData<?> raw : originalData)
        {
            var index = raw.getIndex();

            // 跳过被屏蔽的数据
            if (blockedValues.contains(index))
                continue;

            var sv = (SingleValue<Object>) this.getSingle(index);
            if (sv == null)
            {
                throw ExecutionErrorException.forMethod("handleEntityMetadata")
                        .withMessage("Index %s not found for watcher of type %s(%s)".formatted(index, entityType, this.getClass().getSimpleName()))
                        .create();
            }

            if (!raw.getType().equals(sv.type()))
                continue;

            var value = this.readOr(sv, null);
            if (value != null)
                overrideList.add(new EntityData<>(sv.index(), sv.type(), value));
            else
                overrideList.add(raw);
        }

        return overrideList;
    }

    protected List<EntityData<?>> handleEntityMetadata(@Unmodifiable List<EntityData<?>> originalData,
                                                       List<EntityData<?>> currentData) throws ExecutionErrorException
    {
        return currentData;
    }

    public void sync()
    {
        markSilent(syncSilentSource);

        syncedOnce.set(true);
        dirtyValues.clear();

        try
        {
            doSync();
        }
        catch (Throwable t)
        {
            logger.warn("Error occurred while syncing watcher", t);
        }

        unmarkSilent(syncSilentSource);
    }

    protected void doSync()
    {
    }

    // Kept for legacy client usage
    //@Deprecated(forRemoval = true)
    public void writeToCompound(CompoundTag nbt)
    {
    }

    //region Networking

    // 针对构建生成包之前就有customWrite的缓解方案: RenderRegistry#register(Player player, RegisterParameters registerParameters)
    // 或许需要找一种办法能让SingleWatcher在初始化值的时候不要发送任何数据包
    private final Collection<Object> silentRequestSources = Collections.synchronizedList(new ObjectArrayList<>());

    public void markSilent(Object source)
    {
        silentRequestSources.add(source);
    }

    public void unmarkSilent(Object source)
    {
        //workaround: fastutil: If the list is empty, calling remove will throw exception?!
        /*
            [20:54:14 WARN]: [com.github.retrooper.packetevents.PacketEventsAPI] PacketEvents caught an unhandled exception while calling your listener.
            java.lang.IndexOutOfBoundsException: Index (0) is greater than or equal to list size (0)
            at it.unimi.dsi.fastutil.objects.ObjectArrayList.remove(ObjectArrayList.java:402) ~[fastutil-8.5.15.jar:?]
            at it.unimi.dsi.fastutil.objects.ObjectArrayList.remove(ObjectArrayList.java:416) ~[fastutil-8.5.15.jar:?]
            at feathermorph-2.0.0.rc3-1.21.4-final.jar/xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher.unmarkSilent(SingleWatcher.java:502) ~[feathermorph-2.0.0.rc3-1.21.4-final.jar:?]
            at feathermorph-2.0.0.rc3-1.21.4-final.jar/xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher.sync(SingleWatcher.java:474) ~[feathermorph-2.0.0.rc3-1.21.4-final.jar:?]
            at feathermorph-2.0.0.rc3-1.21.4-final.jar/xyz.nifeather.morph.backends.server.renderer.network.PacketFactory.buildFullMetaPacket(PacketFactory.java:47) ~[feathermorph-2.0.0.rc3-1.21.4-final.jar:?]
        */
        if (silentRequestSources.isEmpty())
            return;

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

    protected void sendPacketToAffectedPlayers(PacketWrapper<?> packet)
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

        var protocol = PacketEvents.getAPI().getPlayerManager();
        players.forEach(p -> protocol.sendPacket(p, packet));
    }

    public abstract List<PacketWrapper<?>> buildSpawnPackets() throws BuildFailedException;

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
            logger.warn("Error occurred while disposing", t);
        }
    }

    protected void onDispose()
    {
    }
}
