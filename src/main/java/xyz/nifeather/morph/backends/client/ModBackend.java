package xyz.nifeather.morph.backends.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Messages.FormattableMessage;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.backends.DisguiseBackend;
import xyz.nifeather.morph.backends.DisguiseWrapper;
import xyz.nifeather.morph.backends.WrapperProperties;
import xyz.nifeather.morph.messages.strings.BackendStrings;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.ExecutionErrorException;
import xyz.nifeather.morph.misc.ModNetworkingHelper;
import xyz.nifeather.morph.network.commands.S2C.clientrender.S2CCRRegisterCommand;
import xyz.nifeather.morph.network.commands.S2C.clientrender.S2CCRSyncRegisterCommand;
import xyz.nifeather.morph.network.commands.S2C.clientrender.S2CCRUnregisterCommand;
import xyz.nifeather.morph.network.server.MorphClientHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ModBackend extends DisguiseBackend<TrackingClientDisguise, ModDisguiseWrapper>
{
    /**
     * Gets the identifier of this backend.
     *
     * @return An identifier of this backend.
     */
    @Override
    public String getIdentifier()
    {
        return "client";
    }

    @Override
    public FormattableMessage getDisplayName()
    {
        return BackendStrings.clientBackendName();
    }

    @Override
    public DisguiseWrapper<TrackingClientDisguise> createInstance(EntityType entityType)
    {
        return new ModDisguiseWrapper(new TrackingClientDisguise(entityType), this);
    }

    @Override
    public DisguiseWrapper<TrackingClientDisguise> createPlayerInstance(String targetPlayerName)
    {
        var wrapper = new ModDisguiseWrapper(new TrackingClientDisguise(EntityType.PLAYER), this);
        wrapper.setDisguiseName(targetPlayerName);

        return wrapper;
    }

    @Override
    public TrackingClientDisguise createRawInstance(Entity entity)
    {
        return new TrackingClientDisguise(entity.getType());
    }

    @Override
    public boolean isDisguised(@Nullable Entity target)
    {
        return playerFallbackWrapperMap.containsKey(target);
    }

    @Override
    public ModDisguiseWrapper getWrapper(Entity target)
    {
        if (!(target instanceof Player player)) return null;

        return playerFallbackWrapperMap.getOrDefault(player, null);
    }

    /**
     * 从给定的Wrapper克隆一个属于此后端的新Wrapper
     *
     * @param otherWrapper 可能属于其他后端的Wrapper
     * @return 一个新的属于此后端的Wrapper
     */
    @Override
    public @NotNull ModDisguiseWrapper cloneWrapperFrom(DisguiseWrapper<?> otherWrapper)
    {
        return otherWrapper instanceof ModDisguiseWrapper modDisguiseWrapper
                ? cloneWrapper(modDisguiseWrapper)
                : cloneOther(otherWrapper);
    }

    private ModDisguiseWrapper cloneWrapper(ModDisguiseWrapper other)
    {
        return (ModDisguiseWrapper) other.clone();
    }

    private ModDisguiseWrapper cloneOther(DisguiseWrapper<?> other)
    {
        return ModDisguiseWrapper.fromExternal(other, this);
    }

    private final Map<Player, ModDisguiseWrapper> playerFallbackWrapperMap = new Object2ObjectOpenHashMap<>();

    @Resolved(shouldSolveImmediately = true)
    private ModNetworkingHelper modNetworkingHelper;

    ModNetworkingHelper getNetworkingHelper()
    {
        return modNetworkingHelper;
    }

    @Override
    public void disguise(Player player, DisguiseWrapper<?> rawWrapper) throws ExecutionErrorException
    {
        if (!(rawWrapper instanceof ModDisguiseWrapper wrapper))
        {
            throw ExecutionErrorException.forMethod("ModBackend#disguise")
                    .withMessage("The given disguise wrapper is not an instance of ModDisguiseWrapper.")
                    .create();
        }

        if (playerFallbackWrapperMap.containsKey(player))
            unDisguise(player);

        //发送元数据

        var players = new ObjectArrayList<>(featherMorph().getPlatform().onlinePlayersNative());
        players.remove(player);
        var cmd = new S2CCRRegisterCommand(player.getEntityId(), wrapper.readPropertyOrThrow(WrapperProperties.DISGUISE_ID));
        players.forEach(p -> clientHandler.sendCommand(p, cmd));

        modNetworkingHelper.prepareMeta(player)
                .forWrapper(rawWrapper)
                .send();

        wrapper.setBindingPlayer(player);

        playerFallbackWrapperMap.put(player, wrapper);
    }

    public S2CCRSyncRegisterCommand generateRenderSyncCommand(MorphManager morphManager)
    {
        var map = new HashMap<Integer, String>();
        for (DisguiseState disguiseState : morphManager.getActiveDisguises())
        {
            var player = disguiseState.getPlayer();
            map.put(player.getEntityId(), disguiseState.getDisguiseIdentifier());
        }

        return S2CCRSyncRegisterCommand.of(map);
    }

    @Override
    public void onClientModInitialize(Player player, MorphClientHandler clientHandler, MorphManager morphManager)
    {
        clientHandler.sendCommand(player, this.generateRenderSyncCommand(morphManager));

        // Sync disguises to the client
        var disguises = morphManager.getActiveDisguises();
        for (DisguiseState bindingState : disguises)
        {
            var bindingPlayer = bindingState.getPlayer();

            var packet = modNetworkingHelper.prepareMeta(bindingPlayer)
                    .forDisguiseState(bindingState)
                    .build();

            clientHandler.sendCommand(player, packet);
        }
    }

    @Resolved
    private MorphClientHandler clientHandler;

    @Override
    public void respawnDisguise(DisguiseWrapper<?> wrapper)
    {
    }

    @Override
    public boolean unDisguise(Player player)
    {
        var wrapper = playerFallbackWrapperMap.getOrDefault(player, null);

        if (wrapper != null)
            wrapper.dispose();

        var cmd = new S2CCRUnregisterCommand(player.getEntityId());
        var players = new ObjectArrayList<>(featherMorph().getPlatform().onlinePlayersNative());
        players.remove(player);
        players.forEach(p -> clientHandler.sendCommand(p, cmd));

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
    public @Nullable ModDisguiseWrapper fromOfflineSave(String offlineParameter)
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
    public Collection<ModDisguiseWrapper> listInstances()
    {
        return playerFallbackWrapperMap.values();
    }
}
