package xyz.nifeather.morph.storage.offlinestore;

import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.interfaces.IManageSavedDisguise;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.storage.DirectoryJsonBasedStorage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SavedDisguiseStore extends DirectoryJsonBasedStorage<SavedDisguise> implements IManageSavedDisguise
{
    public SavedDisguiseStore()
    {
        super("saved_disguises");
    }

    @Override
    protected SavedDisguise getDefault()
    {
        return new SavedDisguise();
    }

    @Override
    public boolean save(@NotNull DisguiseState state)
    {
        return save(state, "anonymous:" + state.getPlayerUUID().toString());
    }

    @Override
    public boolean save(@NotNull DisguiseState state, String name)
    {
        var file = this.getFile(name, true);
        if (file == null) return false;

        String json = gson.toJson(SavedDisguise.fromState(state));

        try
        {
            FileUtils.writeStringToFile(file, json, StandardCharsets.UTF_8, false);
        }
        catch (IOException e)
        {
            logger.error("SavedDisguiseStore: Can't write content to disk", e);
            return false;
        }

        return true;
    }

    @Override
    public List<String> listNames()
    {
        return Arrays.stream(directoryStorage.getFiles())
                .map(this::getKeyFromFile)
                .toList();
    }

    /**
     * Read saved disguise from the disk
     * @param name Name of the target save
     * @return An instance of {@link SavedDisguise}, null if not found
     */
    @Nullable
    public SavedDisguise read(String name)
    {
        var file = this.getFile(name, true);
        if (file == null || !file.exists())
            return null;

        try
        {
            var content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

            return gson.fromJson(content, SavedDisguise.class);
        }
        catch (JsonSyntaxException e)
        {
            logger.error("SavedDisguiseStore: Failed to convert content to JSON string, malformed file!", e);
            return null;
        }
        catch (IOException e)
        {
            logger.error("SavedDisguiseStore: Failed to read SavedDisguise from disk", e);
            return null;
        }
    }

    @Nullable
    public SavedDisguise read(UUID uuid)
    {
        return read("anonymous:" + uuid.toString());
    }

    public boolean drop(String name)
    {
        var file = this.getFile(name, true);
        if (file == null || !file.exists())
            return false;

        return file.delete();
    }

    public boolean drop(UUID uuid)
    {
        return drop("anonymous:" + uuid.toString());
    }
}
