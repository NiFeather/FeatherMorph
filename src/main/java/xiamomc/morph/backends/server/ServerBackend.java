package xiamomc.morph.backends.server;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.backends.DisguiseBackend;
import xiamomc.morph.backends.DisguiseWrapper;
import xiamomc.morph.backends.server.renderer.ServerRenderer;
import xiamomc.morph.messages.BackendStrings;
import xiamomc.morph.utilities.NbtUtils;
import xiamomc.pluginbase.Messages.FormattableMessage;

import javax.annotation.Nullable;
import java.util.Arrays;
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
     * Creates a disguise from the giving entity
     *
     * @param targetEntity The entity used to construct disguise
     * @return A wrapper that handles the constructed disguise
     */
    @Override
    public DisguiseWrapper<ServerDisguise> createInstance(@NotNull Entity targetEntity)
    {
        var wrapper = new ServerDisguiseWrapper(new ServerDisguise(targetEntity.getType()), this);
        if (targetEntity instanceof Player player)
            wrapper.setDisguiseName(player.getName());

        return wrapper;
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
    public ServerDisguiseWrapper cloneWrapperFrom(DisguiseWrapper<?> otherWrapper)
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
     * @param player  目标玩家
     * @param wrapper 目标Wrapper
     * @return 操作是否成功
     * @apiNote 传入的wrapper可能不是此后端产出的Wrapper，需要对其进行验证
     */
    @Override
    public boolean disguise(Player player, DisguiseWrapper<?> wrapper)
    {
        if (!(wrapper instanceof ServerDisguiseWrapper serverDisguiseWrapper)) return false;
        if (disguiseWrapperMap.containsKey(player.getUniqueId()))
            unDisguise(player);

        disguiseWrapperMap.put(player.getUniqueId(), serverDisguiseWrapper);

        var watcher = serverRenderer.registerEntity(
                player, wrapper.getEntityType(), wrapper.getDisguiseName());

        serverDisguiseWrapper.setRenderParameters(player, watcher);
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
        serverRenderer.unRegisterEntity(player);

        var uuid = player.getUniqueId();
        var wrapper = disguiseWrapperMap.getOrDefault(uuid, null);
        if (wrapper != null)
            wrapper.dispose();

        disguiseWrapperMap.remove(uuid);
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

        CompoundTag compoundTag;

        var typeMatch = Arrays.stream(EntityType.values()).filter(
                t -> t != EntityType.UNKNOWN && t.getKey().asString().equals(typeId)
        ).findFirst().orElse(null);

        if (typeMatch == null)
        {
            logger.warn("Invalid EntityType: '%s'".formatted(typeId));
            return null;
        }

        try
        {
            compoundTag = NbtUtils.toCompoundTag(snbt);
        }
        catch (Throwable t)
        {
            logger.error("Unable to parse sNBT: " + t.getMessage());
            logger.error("Raw string: '%s'".formatted(snbt));
            return null;
        }

        var instance = new ServerDisguise(typeMatch);
        var wrapper = new ServerDisguiseWrapper(instance, this);
        wrapper.mergeCompound(compoundTag);

        return wrapper;
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

        var compound = serverWrapper.getCompound();
        var nbtStr = NbtUtils.getCompoundString(compound);
        var type = wrapper.getEntityType().getKey().asString();
        return "%s@%s".formatted(type, nbtStr);
    }
}
