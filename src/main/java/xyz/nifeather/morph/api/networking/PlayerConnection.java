package xyz.nifeather.morph.api.networking;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.api.FeatherMorphAPI;
import xyz.nifeather.morph.network.server.MorphClientHandler;

import java.util.concurrent.CompletableFuture;

public class PlayerConnection
{
    private final FeatherMorphAPI api;
    private final MorphClientHandler clientHandler;

    public PlayerConnection(FeatherMorphAPI api)
    {
        this.api = api;
        clientHandler = api.directAccess().clientHandler();
    }

    /**
     * @return Whether this player has finished mod login
     */
    public boolean playerLoggedIn(Player player)
    {
        return clientHandler.isPlayerInitialized(player);
    }

    /**
     * @return The given player's command API version, -1 if unknown
     */
    public int getPlayerAPIVersion(Player player)
    {
        return clientHandler.getPlayerVersion(player);
    }

    /**
     * Disconnect the player for the given reason
     */
    public void disconnectPlayer(Player player, @Nullable Exception reason)
    {
        clientHandler.disconnect(player, reason);
    }

    /**
     * @return The version of plugin's implementing command API
     */
    public int getTargetAPI()
    {
        return clientHandler.targetApiVersion;
    }

    /**
     * Get a pending {@link CompletableFuture} that matches the given player.<br>
     * The Future will complete when the player registered all channels required for client-server communication.<br>
     * For possible exceptions that this CompletableFuture might throw, see {@link xyz.nifeather.morph.api.networking.exceptions} package.<br>
     */
    public CompletableFuture<Player> getPlayerChannelPendingFuture(Player player)
    {
        return clientHandler.getPlayerChannelPendingFuture(player);
    }

    /**
     * Get a pending {@link CompletableFuture} that matches the given player.<br>
     * The Future will complete when the player finished mod login.<br>
     * For possible exceptions that this CompletableFuture might throw, see {@link xyz.nifeather.morph.api.networking.exceptions} package.
     */
    public CompletableFuture<Player> getPlayerLoginPendingFuture(Player player)
    {
        return clientHandler.getPlayerLoginPendingFuture(player);
    }

    /**
     * Get a pending {@link CompletableFuture} that matches this player's client connection.<br>
     * This CompletableFuture will <b>NEVER</b> get finished, it only throws exceptions when {@link MorphClientHandler#disconnect(Player, Exception)} is called.<br>
     * For possible exceptions that this CompletableFuture might throw, see {@link xyz.nifeather.morph.api.networking.exceptions} package.
     */
    public CompletableFuture<Player> getPlayerConnectionFuture(Player player)
    {
        return clientHandler.getPlayerConnectionFuture(player);
    }
}
