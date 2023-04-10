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
    @Override
    public DisguiseWrapper<NilDisguise> createInstance(@NotNull Entity targetEntity)
    {
        var wrapper = new NilWrapper(new NilDisguise(targetEntity.getType()));
        wrapper.setDisguiseName(targetEntity.getName());

        return wrapper;
    }

    @Override
    public DisguiseWrapper<NilDisguise> createInstance(EntityType entityType)
    {
        return new NilWrapper(new NilDisguise(entityType));
    }

    @Override
    public DisguiseWrapper<NilDisguise> createPlayerInstance(String targetPlayerName)
    {
        var wrapper = new NilWrapper(new NilDisguise(EntityType.PLAYER));
        wrapper.setDisguiseName(targetPlayerName);

        return wrapper;
    }

    @Override
    public DisguiseWrapper<NilDisguise> fromOfflineString(String offlineStr)
    {
        return null;
    }

    @Override
    public NilDisguise createRawInstance(Entity entity)
    {
        return new NilDisguise(entity.getType());
    }

    @Override
    public boolean isDisguised(Entity target)
    {
        return getDisguise(target) != null;
    }

    @Override
    public NilWrapper getDisguise(Entity target)
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
}
