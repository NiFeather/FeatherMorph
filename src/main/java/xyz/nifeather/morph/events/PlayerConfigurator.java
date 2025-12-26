package xyz.nifeather.morph.events;

import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.interfaces.IManagePlayerData;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.storage.playerdata.PlayerMeta;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PlayerConfigurator extends MorphPluginObject implements Listener
{
    @Resolved(shouldSolveImmediately = true)
    private MorphManager morphManager;

    @Resolved(shouldSolveImmediately = true)
    private MorphClientHandler clientHandler;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void postPlayerConfiguration(AsyncPlayerConnectionConfigureEvent e)
    {
        var uuid = e.getConnection().getProfile().getId();

        if (uuid == null)
        {
            logger.warn("The server have an incoming player connection, but don't know their UUID! Not pulling disguise data...");
            return;
        }

        var future = morphManager.loadPlayerDataAsync(uuid);
        future.thenAccept(meta -> this.onPlayerMeta(uuid, meta));
    }

    private void onPlayerMeta(UUID uuid, PlayerMeta playerMeta)
    {
        var player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        clientHandler.refreshPlayerClientMorphs(playerMeta.getUnlockedDisguiseIdentifiers(), player);
    }
}
