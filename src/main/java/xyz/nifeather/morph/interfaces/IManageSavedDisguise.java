package xyz.nifeather.morph.interfaces;

import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.storage.offlinestore.SavedDisguise;

import java.util.List;
import java.util.UUID;

public interface IManageSavedDisguise
{
    /**
     * Save the giving DisguiseState to the disk.
     */
    public boolean save(DisguiseState state);

    /**
     * Read an offline disguise from the disk, null if not available(failed/not found/inaccessible)
     */
    @Nullable
    public SavedDisguise read(UUID uuid);

    public List<String> listNames();
}
