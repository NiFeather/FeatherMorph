package xiamomc.morph.backends.fallback;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.backends.DisguiseBackend;
import xiamomc.morph.backends.DisguiseWrapper;

import java.util.Map;

public class NilBackend extends DisguiseBackend<NilDisguise, NilWrapper>
{
    /**
     * Gets the identifier of this backend.
     *
     * @return An identifier of this backend.
     */
    @Override
    public String getIdentifier()
    {
        return "nil";
    }

    @Override
    public DisguiseWrapper<NilDisguise> createInstance(@NotNull Entity targetEntity)
    {
        var wrapper = new NilWrapper(new NilDisguise(targetEntity.getType()), this);
        wrapper.setDisguiseName(targetEntity.getName());

        return wrapper;
    }

    @Override
    public DisguiseWrapper<NilDisguise> createInstance(EntityType entityType)
    {
        return new NilWrapper(new NilDisguise(entityType), this);
    }

    @Override
    public DisguiseWrapper<NilDisguise> createPlayerInstance(String targetPlayerName)
    {
        var wrapper = new NilWrapper(new NilDisguise(EntityType.PLAYER), this);
        wrapper.setDisguiseName(targetPlayerName);

        return wrapper;
    }

    @Override
    public NilDisguise createRawInstance(Entity entity)
    {
        return new NilDisguise(entity.getType());
    }

    @Override
    public boolean isDisguised(@Nullable Entity target)
    {
        return playerFallbackWrapperMap.containsKey(target);
    }

    @Override
    public NilWrapper getWrapper(Entity target)
    {
        if (!(target instanceof Player player)) return null;

        return playerFallbackWrapperMap.getOrDefault(player, null);
    }

    private final Map<Player, NilWrapper> playerFallbackWrapperMap = new Object2ObjectOpenHashMap<>();

    @Override
    public boolean disguise(Player player, DisguiseWrapper<?> rawWrapper)
    {
        if (!(rawWrapper instanceof NilWrapper wrapper))
            return false;

        playerFallbackWrapperMap.put(player, wrapper);
        return true;
    }

    @Override
    public boolean unDisguise(Player player)
    {
        var wrapper = playerFallbackWrapperMap.getOrDefault(player, null);
        playerFallbackWrapperMap.remove(player);

        return true;
    }

    /**
     * Deserialize a wrapper instance from the giving parameter
     *
     * @param offlineParameter The parameter to deserialize
     * @return A wrapper that presents the giving parameter.
     * null if invalid or illegal
     */
    @Override
    public @Nullable NilWrapper fromOfflineSave(String offlineParameter)
    {
        return null;
    }

    /**
     * Serialize a wrapper instance to a string that can be saved in the Offline Storage
     *
     * @param wrapper The target wrapper to save
     * @return A serialized string that can be deserialized to a wrapper in the future.
     * Null if the giving wrapper is not supported by this backend.
     */
    @Override
    public @Nullable String toOfflineSave(DisguiseWrapper<?> wrapper)
    {
        return null;
    }
}
