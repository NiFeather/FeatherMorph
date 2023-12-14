package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.AbstractValues;
import xiamomc.morph.backends.server.renderer.network.datawatcher.values.SingleValue;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class SingleWatcher extends MorphPluginObject
{
    protected void initRegistry()
    {
    }

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
        initRegistry();

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

    protected void onCustomWrite(RegistryKey<?> key, @Nullable Object oldVal, Object newVal)
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
        write(singleValue.index(), value);
    }

    public void write(int index, Object value)
    {
        var single = getSingle(index);
        if (single == null)
            throw new NullDependencyException("No registry found for index '%s'".formatted(index));

        if (!single.defaultValue().getClass().isInstance(value))
            throw new IllegalArgumentException("Incompatable value for index '%s', excepted for '%s', but got '%s'".formatted(index, single.defaultValue().getClass(), value.getClass()));

        var prev = registry.getOrDefault(single, null);
        registry.put(single, value);
        dirtySingles.put(single, value);

        onTrackerWrite(index, prev, value);

        if (!syncing)
            sendPacketToAffectedPlayers(packetFactory.buildDiffMetaPacket(getBindingPlayer(), this));
    }

    @Resolved(shouldSolveImmediately = true)
    private PacketFactory packetFactory;

    protected PacketFactory getPacketFactory()
    {
        return packetFactory;
    }

    protected void onTrackerWrite(int index, Object oldVal, Object newVal)
    {
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

    private boolean syncing;

    public void sync()
    {
        syncing = true;

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

    //region Networking

    //endregion Networking

    protected List<Player> getAffectedPlayers(Player sourcePlayer)
    {
        var players = sourcePlayer.getWorld().getPlayers();
        players.remove(sourcePlayer);
        if (NmsRecord.ofPlayer(sourcePlayer).gameMode.getGameModeForPlayer() == GameType.SPECTATOR)
        {
            players.removeIf(bukkitPlayer ->
                    NmsRecord.ofPlayer(bukkitPlayer).gameMode.getGameModeForPlayer() != GameType.SPECTATOR);
        }

        return players;
    }

    protected void sendPacketToAffectedPlayers(PacketContainer packet)
    {
        var players = getAffectedPlayers(getBindingPlayer());

        var protocol = ProtocolLibrary.getProtocolManager();
        players.forEach(p -> protocol.sendServerPacket(p, packet));
    }
}
