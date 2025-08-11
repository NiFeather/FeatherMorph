package xyz.nifeather.morph.api.utilties.v0;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.api.direct.FeatherMorphDirectAccess;
import xyz.nifeather.morph.backends.server.ServerDisguiseWrapper;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.network.server.MessageChannel;

import java.util.UUID;

@ApiStatus.Experimental
public class UtilitiesAlpha
{
    private final FeatherMorphDirectAccess directAccess;

    public UtilitiesAlpha(FeatherMorphDirectAccess directAccess)
    {
        this.directAccess = directAccess;
    }

    public boolean isPlayerDisguising(Player player)
    {
        var morphManager = directAccess.morphManager();

        return morphManager.getDisguiseStateFor(player) != null;
    }

    /**
     * Lookup if the given UUID matches a player's disguise
     * @param input The UUID of the disguise
     * @return The disguising player that matches the input UUID, NULL if not found
     */
    @Nullable
    public Player lookupPlayerFromDisguiseUUID(UUID input)
    {
        var morphManager = directAccess.morphManager();

        for (DisguiseState state : morphManager.getActiveDisguises())
        {
            // Currently, only ServerDisguiseWrappers are supported
            if (!(state.getDisguiseWrapper() instanceof ServerDisguiseWrapper wrapper))
                continue;

            var watcher = wrapper.getBindingWatcher();
            if (watcher == null)
                continue;

            var uuid = watcher.readEntryOrDefault(CustomEntries.SPAWN_UUID, null);
            if (uuid == null)
                continue;

            if (uuid.equals(input))
                return state.getPlayer();
        }

        return null;
    }

    /**
     * Check if the backend is ServerBackend
     * @return TRUE if the backend is the server backend, FALSE if not
     */
    public boolean isServerBackend()
    {
        var morphManager = directAccess.morphManager();

        return morphManager.getDefaultBackend().getIdentifier().equals("server");
    }

    /**
     * Find UUID of the disguise from the given player
     * @param input The player to lookup
     * @return The disguise UUID, if available
     * @apiNote If the server is not running ServerBackend, this will return NULL.
     *          To check whether the server is running ServerBackend, use {@link #isServerBackend()}.
     */
    @Nullable
    public UUID lookupDisguiseUUIDFromPlayer(Player input)
    {
        var morphManager = directAccess.morphManager();

        var state = morphManager.getDisguiseStateFor(input);
        if (state == null)
            return null;

        var rawWrapper = state.getDisguiseWrapper();

        if (!(rawWrapper instanceof ServerDisguiseWrapper wrapper))
            return null;

        var watcher = wrapper.getBindingWatcher();
        if (watcher == null)
            return null;

        return wrapper.getBindingWatcher().readEntryOrDefault(CustomEntries.SPAWN_UUID, null);
    }

    /**
     * Get channels that we currently prefer to communicate with clients
     */
    public String[] getPreferredPluginChannels()
    {
        return MessageChannel.preferredChannels();
    }

    /**
     * Get all plugin channels that we support, including deprecated(legacy) channels
     */
    public String[] getAllPluginChannels()
    {
        return MessageChannel.allValidChannels();
    }
}
