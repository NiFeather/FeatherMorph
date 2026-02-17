package xyz.nifeather.morph.storage.offlinestore;

import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.FileUtils;
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

    /**
     * 将一个玩家的DisguiseState推到存储里
     * @param state DisguiseState
     */
    public boolean save(DisguiseState state)
    {
        var uniqueId = state.getPlayer().getUniqueId();
        String uuidString = uniqueId.toString();

        var file = directoryStorage.getFile(uuidString + ".json", true);
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
                .map(f -> f.getName().replace(".json", ""))
                .toList();
    }

    /**
     * 从存储里取出离线State并从池里移除此State
     * @param uuid 玩家UUID
     * @return 离线State
     */
    @Nullable
    public SavedDisguise read(UUID uuid)
    {
        var file = directoryStorage.getFile(uuid.toString() + ".json", false);
        if (file == null || !file.exists())
            return null;

        try
        {
            var content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            var instance = gson.fromJson(content, SavedDisguise.class);

            file.delete();
            return instance;
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
}
