package xyz.nifeather.morph.backends.server;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.backends.DisguiseBackend;
import xyz.nifeather.morph.backends.DisguiseWrapper;
import xyz.nifeather.morph.backends.server.renderer.ServerRenderer;
import xyz.nifeather.morph.messages.strings.BackendStrings;
import xyz.nifeather.morph.misc.BuildFailedException;
import xyz.nifeather.morph.misc.ExecutionErrorException;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class ServerBackend extends DisguiseBackend<ServerDisguise, ServerDisguiseWrapper>
{
    @Nullable
    public static ServerBackend getInstance()
    {
        return instance;
    }

    private static ServerBackend instance;

    public final ServerRenderer serverRenderer;

    public ServerBackend()
    {
        instance = this;
        serverRenderer = new ServerRenderer();
    }

    @Override
    public void dispose()
    {
        serverRenderer.dispose();
    }

    /**
     * Gets the identifier of this backend.
     *
     * @return An identifier of this backend.
     */
    @Override
    public String getIdentifier()
    {
        return "server";
    }

    @Override
    public FormattableMessage getDisplayName()
    {
        return BackendStrings.serverBackendName();
    }

    /**
     * Creates a disguise by the giving type
     *
     * @param entityType Target entity type
     * @return A wrapper that handles the constructed disguise
     */
    @Override
    public DisguiseWrapper<ServerDisguise> createInstance(EntityType entityType)
    {
        return new ServerDisguiseWrapper(new ServerDisguise(entityType), this);
    }

    /**
     * Creates a player disguise by the giving name
     *
     * @param targetPlayerName Target player name
     * @return A wrapper that handles the constructed disguise
     */
    @Override
    public DisguiseWrapper<ServerDisguise> createPlayerInstance(String targetPlayerName)
    {
        var wrapper = new ServerDisguiseWrapper(new ServerDisguise(EntityType.PLAYER), this);
        wrapper.setDisguiseName(targetPlayerName);

        return wrapper;
    }

    /**
     * Creates a disguise instance directly from the entity
     *
     * @param entity The entity used to construct disguise
     * @return The constructed instance
     */
    @Override
    public ServerDisguise createRawInstance(Entity entity)
    {
        return new ServerDisguise(entity.getType());
    }

    private final Map<UUID, ServerDisguiseWrapper> disguiseWrapperMap = new Object2ObjectOpenHashMap<>();

    /**
     * Checks whether an entity is disguised by this backend
     *
     * @param target The entity to check
     * @return Whether this entity is disguised by this backend
     */
    @Override
    public boolean isDisguised(@org.jetbrains.annotations.Nullable Entity target)
    {
        if (target == null) return false;
        return disguiseWrapperMap.containsKey(target.getUniqueId());
    }

    /**
     * Gets the wrapper that handles the target entity's disguise instance
     *
     * @param target The entity to lookup
     * @return The wrapper that handles the entity's disguise. Null if it's not disguised.
     */
    @Override
    public @Nullable ServerDisguiseWrapper getWrapper(Entity target)
    {
        return disguiseWrapperMap.getOrDefault(target.getUniqueId(), null);
    }

    /**
     * 从给定的Wrapper克隆一个属于此后端的新Wrapper
     *
     * @param otherWrapper 可能属于其他后端的Wrapper
     * @return 一个新的属于此后端的Wrapper
     */
    @Override
    public @NotNull ServerDisguiseWrapper cloneWrapperFrom(DisguiseWrapper<?> otherWrapper)
    {
        return otherWrapper instanceof ServerDisguiseWrapper serverDisguiseWrapper
                ? cloneWrapper(serverDisguiseWrapper)
                : cloneOther(otherWrapper);
    }

    private ServerDisguiseWrapper cloneWrapper(ServerDisguiseWrapper other)
    {
        return (ServerDisguiseWrapper) other.clone();
    }

    private ServerDisguiseWrapper cloneOther(DisguiseWrapper<?> other)
    {
        return ServerDisguiseWrapper.cloneFromExternal(other, this);
    }

    /**
     * 将某一玩家伪装成给定Wrapper中的实例
     *
     * @param entity
     * @param wrapper 目标Wrapper
     * @throws ExecutionErrorException If there's an error occurred while applying
     * @apiNote 传入的wrapper可能不是此后端产出的Wrapper，需要对其进行验证
     */
    @Override
    public void disguise(LivingEntity entity, DisguiseWrapper<?> wrapper) throws ExecutionErrorException
    {
        if (!(wrapper instanceof ServerDisguiseWrapper serverDisguiseWrapper))
        {
            throw ExecutionErrorException.forMethod("ServerBackend#disguise")
                    .withMessage("The given disguise wrapper is not an instance of ServerDisguiseWrapper.")
                    .create();
        }

        if (disguiseWrapperMap.containsKey(entity.getUniqueId()))
            unDisguise(entity, false);

        disguiseWrapperMap.put(entity.getUniqueId(), serverDisguiseWrapper);

        var watcher = serverRenderer.registerEntity(
                entity, wrapper.getEntityType(), wrapper.getDisguiseName());

        watcher.markSilent(this);
        serverDisguiseWrapper.setRenderParameters(entity, watcher);
        watcher.unmarkSilent(this);

        try
        {
            serverRenderer.spawnVirtualEntity(entity.getUniqueId());
        }
        catch (NullDependencyException e)
        {
            throw ExecutionErrorException.forMethod("ServerBackend#disguise")
                    .causedBy(e)
                    .withMessage("Failed to refresh entity state, watcher not registered in the renderer!")
                    .create();
        }
        catch (BuildFailedException e)
        {
            if (!e.critical())
            {
                logger.warn("Renderer failed to build spawn packets, ignoring: ", e);
                return;
            }

            throw ExecutionErrorException.forMethod("ServerBackend#disguise")
                    .causedBy(e)
                    .withMessage("Renderer failed to build spawn packets")
                    .create();
        }
        catch (Exception e)
        {
            throw ExecutionErrorException.forMethod("ServerBackend#disguise")
                    .causedBy(e)
                    .withMessage("Unknown error")
                    .create();
        }
    }

    private boolean unDisguise(LivingEntity entity, boolean unregisterFromRenderer)
    {
        if (unregisterFromRenderer)
            serverRenderer.unRegisterEntity(entity);

        var uuid = entity.getUniqueId();
        var wrapper = disguiseWrapperMap.getOrDefault(uuid, null);
        if (wrapper != null)
            wrapper.dispose();

        disguiseWrapperMap.remove(uuid);
        return true;
    }

    /**
     * Undisguise a player
     *
     * @param entity@return Whether the operation was successful
     */
    @Override
    public boolean unDisguise(LivingEntity entity)
    {
        return unDisguise(entity, true);
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
    public @Nullable ServerDisguiseWrapper fromOfflineSave(String offlineParameter)
    {
        var sp = offlineParameter.split("\\|", 2);

        if (sp.length < 2)
        {
            logger.warn("Invalid offline parameter: '%s'".formatted(offlineParameter));
            return null;
        }

        if (!sp[0].equals(this.getIdentifier()))
        {
            logger.error("The given parameter is not compatible with current backend. (Expected '%s', Current '%s')"
                    .formatted(sp[0], getIdentifier()));

            return null;
        }

        var spilt = sp[1].split("@", 2);

        if (spilt.length < 2)
        {
            logger.warn("Invalid offline parameter: '%s'".formatted(sp[1]));
            return null;
        }

        var snbt = spilt[1];
        var typeId = spilt[0];

        var typeMatch = Arrays.stream(EntityType.values()).filter(
                t -> t != EntityType.UNKNOWN && t.getKey().asString().equals(typeId)
        ).findFirst().orElse(null);

        if (typeMatch == null)
        {
            logger.warn("Invalid EntityType: '%s'".formatted(typeId));
            return null;
        }

        var instance = new ServerDisguise(typeMatch);

        return new ServerDisguiseWrapper(instance, this);
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
        if (!(wrapper instanceof ServerDisguiseWrapper serverWrapper))
            return null;

        var type = wrapper.getEntityType().getKey().asString();
        return "%s@%s".formatted(type, "NIL");
    }

    @Override
    public Collection<ServerDisguiseWrapper> listInstances()
    {
        return disguiseWrapperMap.values();
    }
}
