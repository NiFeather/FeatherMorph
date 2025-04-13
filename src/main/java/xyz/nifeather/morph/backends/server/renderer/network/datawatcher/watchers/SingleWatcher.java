package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.backends.server.renderer.network.PacketFactory;
import xyz.nifeather.morph.backends.server.renderer.network.ProtocolEquipment;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.AbstractValues;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.values.SingleValue;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;
import xyz.nifeather.morph.backends.server.renderer.network.registries.RenderRegistry;
import xyz.nifeather.morph.backends.server.renderer.utilties.WatcherUtils;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.NmsRecord;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.EntityTypeUtils;
import xyz.nifeather.morph.utilities.NmsUtils;

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

    /**
     * Currently disguise properties are handled by the wrapper.
     * So the watcher only supports writing values so that the watcher could sync them with the wrapper.
     */
    public final <X> void writeProperty(SingleProperty<X> property, X value)
    {
        this.onPropertyWrite(property, value);
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
     * Values in this list shouldn't be included with meta packet processing in {@link xyz.nifeather.morph.backends.server.renderer.network.listeners.MetaPacketListener#rebuildServerMetaPacket(AbstractValues, SingleWatcher, WrapperPlayServerEntityMetadata)}
     */
    private final List<Integer> blockedValues = new ObjectArrayList<>();

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
            String message = "Trying to write a SV that doesn't belongs to this Watcher: '%s'. ";

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

    public void sync()
    {
        markSilent(syncSilentSource);

        syncedOnce.set(true);
        dirtyValues.clear();

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

    public List<PacketWrapper<?>> buildSpawnPackets()
    {
        List<PacketWrapper<?>> packets = new ObjectArrayList<>();
        var player = getBindingPlayer();

        if (this.readEntryOrDefault(CustomEntries.VANISHED, false))
            return packets;

        var disguiseEntityType = this.getEntityType();

        var nmsSpawnType = EntityTypeUtils.getNmsType(disguiseEntityType);
        if (nmsSpawnType == null)
        {
            logger.error("No NMS Type for Bukkit Type '%s'".formatted(disguiseEntityType));
            logger.error("Not building spawn packets!");

            return packets;
        }

        var nmsPlayer = NmsRecord.ofPlayer(player);
        UUID spawnUUID = this.readEntryOrThrow(CustomEntries.SPAWN_UUID);
        if (spawnUUID.equals(Util.NIL_UUID))
            throw new IllegalStateException("A watcher with NIL UUID?!");

        //todo: Should we use a better way to get the yaw/pitch?
        //      I don't want to read yaw/pitch from player directly, so I used OVERLAYED_XXX to generate the value on call, so that other watchers can override the value
        var pitch = this.readEntryOrDefault(CustomEntries.OVERLAYED_PITCH, player.getPitch());
        var yaw = this.readEntryOrDefault(CustomEntries.OVERLAYED_YAW, player.getYaw());

        //生成实体
        var playerMotion = player.getVelocity();
        var spawnPacket = new WrapperPlayServerSpawnEntity(
                this.readEntryOrThrow(CustomEntries.SPAWN_ID), spawnUUID,
                SpigotConversionUtil.fromBukkitEntityType(disguiseEntityType),
                new Location(new Vector3d(player.getX(), player.getY(), player.getZ()), yaw, pitch),
                nmsPlayer.getYHeadRot(), 0,
                new Vector3d(playerMotion.getX(), playerMotion.getY(), playerMotion.getZ())
        );

        packets.add(spawnPacket);

        //生成装备和Meta
        var displayingFakeEquipments = this.readEntryOrDefault(CustomEntries.DISPLAY_FAKE_EQUIPMENT, false);
        var equip = displayingFakeEquipments
                ? this.readEntryOrDefault(CustomEntries.EQUIPMENT, new DisguiseEquipment())
                : player.getEquipment();

        packets.add(new WrapperPlayServerEntityEquipment(player.getEntityId(), ProtocolEquipment.toPEEquipmentList(equip)));

        packets.add(PacketFactory.buildFullMetaPacket(player, this));

        // 载具
        if (player.getVehicle() != null)
        {
            int[] passengers = player.getVehicle().getPassengers()
                    .stream()
                    .mapToInt(Entity::getEntityId)
                    .toArray();

            packets.add(new WrapperPlayServerSetPassengers(player.getVehicle().getEntityId(), passengers));
        }

        if (!player.getPassengers().isEmpty())
        {
            int[] passengers = player.getPassengers()
                    .stream()
                    .mapToInt(Entity::getEntityId)
                    .toArray();

            packets.add(new WrapperPlayServerSetPassengers(player.getEntityId(), passengers));
        }

        // 属性
        if (disguiseEntityType.isAlive())
            packets.add(this.buildAttributePacket());

        return packets;
    }

    private WrapperPlayServerUpdateAttributes.PropertyModifier.Operation fromNMSOperation(AttributeModifier.Operation nmsOperation)
    {
        return switch (nmsOperation)
        {
            case ADD_VALUE -> WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.ADDITION;
            case ADD_MULTIPLIED_BASE -> WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.MULTIPLY_BASE;
            case ADD_MULTIPLIED_TOTAL -> WrapperPlayServerUpdateAttributes.PropertyModifier.Operation.MULTIPLY_TOTAL;
            default -> throw new RuntimeException("Unknown operation: " + nmsOperation);
        };
    }

    private WrapperPlayServerUpdateAttributes buildAttributePacket()
    {
        var player = getBindingPlayer();
        List<WrapperPlayServerUpdateAttributes.Property> attributeProperties = new ObjectArrayList<>();

        var nmsPlayer = NmsRecord.ofPlayer(player);

        List<AttributeInstance> attributes = entityType == EntityType.PLAYER
                ? new ObjectArrayList<>(nmsPlayer.getAttributes().getSyncableAttributes())
                : NmsUtils.getValidAttributes(entityType, nmsPlayer.getAttributes());

        attributes.forEach(instance ->
        {
            // Still NMS :(
            var id = BuiltInRegistries.ATTRIBUTE.getKey(instance.getAttribute().value()).toString();

            var packetAttribute = Attributes.getByName(id);
            if (packetAttribute == null)
            {
                logger.warn("Unknown attribute for packet: " + id);
                return;
            }

            List<WrapperPlayServerUpdateAttributes.PropertyModifier> modifiers = new ObjectArrayList<>();
            for (AttributeModifier modifier : instance.getModifiers())
            {
                var packetModifier = new WrapperPlayServerUpdateAttributes.PropertyModifier(
                        new ResourceLocation(modifier.id().toString()),
                        UUID.randomUUID(),
                        modifier.amount(),
                        fromNMSOperation(modifier.operation())
                );

                modifiers.add(packetModifier);
            }

            var property = new WrapperPlayServerUpdateAttributes.Property(packetAttribute, instance.getBaseValue(), modifiers);
            attributeProperties.add(property);
        });

        return new WrapperPlayServerUpdateAttributes(player.getEntityId(), attributeProperties);
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
