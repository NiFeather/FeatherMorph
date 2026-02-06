package xyz.nifeather.morph.misc.skins;

import com.mojang.authlib.GameProfile;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.storage.DirectoryJsonBasedStorage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SkinStorage extends DirectoryJsonBasedStorage<SingleSkin>
{
    protected SkinStorage()
    {
        super("skins");
    }

    @Override
    protected SingleSkin getDefault()
    {
        return new SingleSkin();
    }

    public synchronized void cache(GameProfile profile)
    {
        var path = this.getPath(profile.name()) + ".json";
        var file = this.directoryStorage.getFile(path, true);
        if (file == null)
        {
            if (FeatherMorphMain.getInstance().debugOutputEnabled())
                logger.warn("[DEBUG] File is null, can't cache skin!");

            return;
        }

        var wrapper = SingleSkin.fromProfile(profile);

        var json = gson.toJson(wrapper);
        try
        {
            FileUtils.writeStringToFile(file, json, StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            logger.error("Unable to write file, can't cache skin.", e);
        }
    }

    public synchronized void delete(String name)
    {
        var path = this.getPath(name) + ".json";
        var file = this.directoryStorage.getFile(path, false);

        if (file == null)
        {
            if (FeatherMorphMain.getInstance().debugOutputEnabled())
                logger.warn("[DEBUG] File is null, can't drop skin!");

            return;
        }

        if (!file.delete())
            logger.warn("Failed to delete cached skin for unknown reason");
    }

    public synchronized void deleteAll()
    {
        for (File file : directoryStorage.getFiles())
        {
            if (file.getName().endsWith(".json"))
                file.delete();
        }
    }

    public record SkinRecord(Optional<GameProfile> profileOptional, boolean expired)
    {
    }

    @Nullable
    SingleSkin getRaw(String name)
    {
        return get(name);
    }

    @NotNull
    public SkinRecord getRecord(String name)
    {
        var single = getRaw(name);

        if (single == null) return new SkinRecord(Optional.empty(), true);

        var profile = single.generateGameProfile();
        return new SkinRecord(
                (profile == null ? Optional.empty() : Optional.of(profile)),
                System.currentTimeMillis() > single.expiresAt);
    }

    //region Utilities

    public List<String> listAvailableNames()
    {
        // TODO: This read ALL skins every time, this is bad!
        return Arrays.stream(directoryStorage.getFiles())
                .map(f -> f.getName().replaceAll(".json", ""))
                .toList();
    }

    //endregion
}
