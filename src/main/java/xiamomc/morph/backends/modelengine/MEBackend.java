package xiamomc.morph.backends.modelengine;

import com.ticxo.modelengine.api.ModelEngineAPI;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.backends.DisguiseBackend;
import xiamomc.morph.backends.DisguiseWrapper;
import xiamomc.morph.backends.modelengine.vanish.IVanishSource;
import xiamomc.morph.backends.modelengine.vanish.ProtocolLibVanishSource;
import xiamomc.morph.backends.modelengine.vanish.VanillaVanishSource;
import xiamomc.pluginbase.Messages.FormattableMessage;

import java.util.Collection;
import java.util.Map;

public class MEBackend extends DisguiseBackend<MEDisguiseInstance, MEDisguiseWrapper>
{
    public MEBackend()
    {
        tryVanishSources();
    }

    /**
     * Gets the identifier of this backend.
     *
     * @return An identifier of this backend.
     */
    @Override
    public String getIdentifier()
    {
        return identifier;
    }

    public final static String identifier = "modelengine";

    @Override
    public FormattableMessage getDisplayName()
    {
        return new FormattableMessage(plugin, "Model Engine Backend");
    }

    @Nullable
    public DisguiseWrapper<MEDisguiseInstance> createInstance(String modelId)
    {
        var registry = ModelEngineAPI.getAPI().getModelRegistry();
        var model = registry.get(modelId);
        if (model == registry.getDefault())
        {
            logger.error("Requested a disguise for an unknown model: {}", modelId);
            return null;
        }

        var instance = new MEDisguiseInstance(modelId);

        return new MEDisguiseWrapper(instance, this);
    }

    /**
     * Creates a disguise from the giving entity
     *
     * @param targetEntity The entity used to construct disguise
     * @return A wrapper that handles the constructed disguise
     */
    @Override
    public DisguiseWrapper<MEDisguiseInstance> createInstance(@NotNull Entity targetEntity)
    {
        throw new NotImplementedException("Disguising as Entity is not available for ModelEngine!");
    }

    /**
     * Creates a disguise by the giving type
     *
     * @param entityType Target entity type
     * @return A wrapper that handles the constructed disguise
     */
    @Override
    public DisguiseWrapper<MEDisguiseInstance> createInstance(EntityType entityType)
    {
        throw new NotImplementedException("Disguising as EntityType is not available for ModelEngine!");
    }

    /**
     * Creates a player disguise by the giving name
     *
     * @param targetPlayerName Target player name
     * @return A wrapper that handles the constructed disguise
     */
    @Override
    public DisguiseWrapper<MEDisguiseInstance> createPlayerInstance(String targetPlayerName)
    {
        throw new NotImplementedException("Disguising as Player is not available for ModelEngine!");
    }

    /**
     * Creates a disguise instance directly from the entity
     *
     * @param entity The entity used to construct disguise
     * @return The constructed instance
     */
    @Override
    public MEDisguiseInstance createRawInstance(Entity entity)
    {
        throw new NotImplementedException("Creating raw instance from entity is not supported!");
    }

    /**
     * Checks whether an entity is disguised by this backend
     *
     * @param target The entity to check
     * @return Whether this entity is disguised by this backend
     */
    @Override
    public boolean isDisguised(@Nullable Entity target)
    {
        if (!(target instanceof Player player)) return false;

        return disguiseWrapperMap.containsKey(player);
    }

    /**
     * Gets the wrapper that handles the target entity's disguise instance
     *
     * @param target The entity to lookup
     * @return The wrapper that handles the entity's disguise. Null if it's not disguised.
     */
    @Override
    public @Nullable MEDisguiseWrapper getWrapper(Entity target)
    {
        if (!(target instanceof Player player)) return null;

        return disguiseWrapperMap.getOrDefault(player, null);
    }

    /**
     * 从给定的Wrapper克隆一个属于此后端的新Wrapper
     *
     * @param otherWrapper 可能属于其他后端的Wrapper
     * @return 一个新的属于此后端的Wrapper
     */
    @Override
    public @NotNull MEDisguiseWrapper cloneWrapperFrom(DisguiseWrapper<?> otherWrapper)
    {
        return otherWrapper instanceof MEDisguiseWrapper meDisguiseWrapper
                ? MEDisguiseWrapper.clone(meDisguiseWrapper, this)
                : MEDisguiseWrapper.cloneOther(otherWrapper, this);

    }

    private final Map<Player, MEDisguiseWrapper> disguiseWrapperMap = new Object2ObjectArrayMap<>();

    private IVanishSource vanishSource;

    private void tryVanishSources()
    {
        try
        {
            vanishSource = new ProtocolLibVanishSource();
            return;
        }
        catch (Throwable t)
        {
            logger.error("Can't use ProtocolLib as a vanish source.");
        }

        vanishSource = new VanillaVanishSource();
    }

    /**
     * 将某一玩家伪装成给定Wrapper中的实例
     *
     * @param player  目标玩家
     * @param rawWrapper 目标Wrapper
     * @return 操作是否成功
     * @apiNote 传入的wrapper可能不是此后端产出的Wrapper，需要对其进行验证
     */
    @Override
    public boolean disguise(Player player, DisguiseWrapper<?> rawWrapper)
    {
        if (!(rawWrapper instanceof MEDisguiseWrapper wrapper))
            return false;

        wrapper.bindPlayer(player);
        var modeled = wrapper.getModeled();
        if (modeled == null)
        {
            logger.warn("Null modeled at where it shouldn't?!");
            return false;
        }

        var active = wrapper.getActiveModel();
        if (active == null)
        {
            logger.warn("Null activeModel at where it shouldn't?!");
            return false;
        }

        vanishSource.vanishPlayer(player);
        disguiseWrapperMap.put(player, wrapper);
        modeled.addModel(active, true);

        return true;
    }

    /**
     * Undisguise a player
     *
     * @param player The player to undisguise
     * @return Whether the operation was successful
     */
    @Override
    public boolean unDisguise(Player player)
    {
        var wrapper = disguiseWrapperMap.getOrDefault(player, null);
        if (wrapper == null) return false;

        var modeled = wrapper.getModeled();
        if (modeled == null)
        {
            logger.warn("Null modeled at where it shouldn't?!");
            return false;
        }

        modeled.destroy();

        logger.info("Model id is " + wrapper.getModelID());
        vanishSource.cancelVanish(player);
        modeled.removeModel(wrapper.getModelID());

        disguiseWrapperMap.remove(player);

        return true;
    }

    /**
     * Deserialize a wrapper instance from the giving parameter
     *
     * @param offlineParameter The parameter to deserialize
     * @return A wrapper that presents the giving parameter.
     * null if invalid or illegal
     * @apiNote The format for the input string is undefined and may looks like one of these three formats: "id|content", "id|*empty*", "*empty*"
     */
    @Override
    public @Nullable MEDisguiseWrapper fromOfflineSave(String offlineParameter)
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

    @Override
    public Collection<MEDisguiseWrapper> listInstances()
    {
        return disguiseWrapperMap.values();
    }
}
