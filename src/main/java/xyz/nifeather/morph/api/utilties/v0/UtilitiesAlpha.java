package xyz.nifeather.morph.api.utilties.v0;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.api.direct.FeatherMorphDirectAccess;
import xyz.nifeather.morph.backends.server.ServerDisguiseWrapper;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.misc.DisguiseState;

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
}
