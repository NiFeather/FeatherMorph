package xyz.nifeather.morph.interfaces;

import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.storage.offlinestore.SavedDisguise;

import java.util.List;
import java.util.UUID;

public interface IManageSavedDisguise
{
    /**
     * Save the giving DisguiseState to the disk using the given name.
     * @return {@code true} if successes
     */
    public boolean save(DisguiseState state, String name);

    /**
     * Save ths given disguise under the `anonymous` directory
     * @return {@code true} if successes
     */
    public boolean save(DisguiseState state);

    /**
     * Read an offline disguise from the disk, null if not available(failed/not found/inaccessible)
     */
    @Nullable
    public SavedDisguise read(String name);

    /**
     * Read a saved disguise under the `anonymous` directory
     * @return An instance of {@link SavedDisguise}, {@code null} if not found
     */
    @Nullable
    public SavedDisguise read(UUID uuid);

    /**
     * List all available saves that can be read by this storage
     */
    public List<String> listNames();
}
