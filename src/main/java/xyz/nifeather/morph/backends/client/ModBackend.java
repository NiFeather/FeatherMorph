package xyz.nifeather.morph.backends.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
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
import xyz.nifeather.morph.messages.BackendStrings;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.ModNetworkingHelper;
import xyz.nifeather.morph.network.commands.S2C.clientrender.S2CCRRegisterCommand;
import xyz.nifeather.morph.network.commands.S2C.clientrender.S2CCRSyncRegisterCommand;
import xyz.nifeather.morph.network.commands.S2C.clientrender.S2CCRUnregisterCommand;
import xyz.nifeather.morph.network.server.MorphClientHandler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ModBackend extends DisguiseBackend<TrackingClientDisguise, ClientDisguiseWrapper>
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
    public DisguiseWrapper<TrackingClientDisguise> createInstance(@NotNull Entity targetEntity)
    {
        var wrapper = new ClientDisguiseWrapper(new TrackingClientDisguise(targetEntity.getType()), this);
        wrapper.setDisguiseName(targetEntity.getName());

        return wrapper;
    }

    @Override
    public DisguiseWrapper<TrackingClientDisguise> createInstance(EntityType entityType)
    {
        return new ClientDisguiseWrapper(new TrackingClientDisguise(entityType), this);
    }

    @Override
    public DisguiseWrapper<TrackingClientDisguise> createPlayerInstance(String targetPlayerName)
    {
        var wrapper = new ClientDisguiseWrapper(new TrackingClientDisguise(EntityType.PLAYER), this);
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
    public ClientDisguiseWrapper getWrapper(Entity target)
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
    public @NotNull ClientDisguiseWrapper cloneWrapperFrom(DisguiseWrapper<?> otherWrapper)
    {
        return otherWrapper instanceof ClientDisguiseWrapper clientDisguiseWrapper
                ? cloneWrapper(clientDisguiseWrapper)
                : cloneOther(otherWrapper);
    }

    private ClientDisguiseWrapper cloneWrapper(ClientDisguiseWrapper other)
    {
        return (ClientDisguiseWrapper) other.clone();
    }

    private ClientDisguiseWrapper cloneOther(DisguiseWrapper<?> other)
    {
        return ClientDisguiseWrapper.fromExternal(other, this);
    }

    private final Map<Player, ClientDisguiseWrapper> playerFallbackWrapperMap = new Object2ObjectOpenHashMap<>();

    @Resolved(shouldSolveImmediately = true)
    private ModNetworkingHelper modNetworkingHelper;

    ModNetworkingHelper getNetworkingHelper()
    {
        return modNetworkingHelper;
    }

    @Override
    public boolean disguise(Player player, DisguiseWrapper<?> rawWrapper)
    {
        if (!(rawWrapper instanceof ClientDisguiseWrapper wrapper))
            return false;

        if (playerFallbackWrapperMap.containsKey(player))
            unDisguise(player);

        //发送元数据

        var players = new ObjectArrayList<>(featherMorph().getPlatform().onlinePlayers());
        players.remove(player);
        var cmd = new S2CCRRegisterCommand(player.getEntityId(), wrapper.readPropertyOrThrow(WrapperProperties.DISGUISE_ID));
        players.forEach(p -> clientHandler.sendCommand(p, cmd));

        modNetworkingHelper.prepareMeta(player)
                .forWrapper(rawWrapper)
                .send();

        wrapper.setBindingPlayer(player);

        playerFallbackWrapperMap.put(player, wrapper);
        return true;
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
    public boolean unDisguise(Player player)
    {
        var wrapper = playerFallbackWrapperMap.getOrDefault(player, null);

        if (wrapper != null)
            wrapper.dispose();

        var cmd = new S2CCRUnregisterCommand(player.getEntityId());
        var players = new ObjectArrayList<>(featherMorph().getPlatform().onlinePlayers());
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
    public @Nullable ClientDisguiseWrapper fromOfflineSave(String offlineParameter)
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
    public Collection<ClientDisguiseWrapper> listInstances()
    {
        return playerFallbackWrapperMap.values();
    }
}
