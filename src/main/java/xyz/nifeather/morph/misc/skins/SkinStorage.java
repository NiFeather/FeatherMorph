package xyz.nifeather.morph.misc.skins;

import com.google.gson.JsonParseException;
import com.mojang.authlib.GameProfile;
import net.minecraft.util.StringUtil;
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

        migrateSkinCacheIfPossible();
    }

    private void migrateSkinCacheIfPossible()
    {
        var oldSkins = new File(FeatherMorphMain.getInstance().getDataFolder(), "stored_skins.json");
        if (!oldSkins.exists()) return;

        logger.info("Migrating legacy cached skins...");

        SkinCacheRoot legacyCachedSkins;
        try
        {
            var content = FileUtils.readFileToString(oldSkins, StandardCharsets.UTF_8);

            legacyCachedSkins = gson.fromJson(content, SkinCacheRoot.class);
        }
        catch (JsonParseException e)
        {
            logger.info("Unable to parse legacy skin cache", e);
            return;
        }
        catch (IOException e)
        {
            logger.error("Can't read file content for legacy skin cache", e);
            return;
        }

        int skinsMigrated = 0;
        int skinsFailedToMigrate = 0;

        for (SingleSkin storedSkin : legacyCachedSkins.storedSkins)
        {
            if (!StringUtil.isValidPlayerName(storedSkin.name))
            {
                skinsFailedToMigrate++;
                continue;
            }

            var profile = storedSkin.generateGameProfile();
            if (profile == null)
            {
                skinsFailedToMigrate++;
                continue;
            }

            if (cache(profile))
                skinsMigrated++;
            else
                skinsFailedToMigrate++;
        }

        if (!oldSkins.renameTo(new File(FeatherMorphMain.getInstance().getDataFolder(), "stored_skins.migrated.json")))
            logger.error("Can't rename skin cache file, please rename or delete it manually!");

        logger.info("Done migrating legacy cached skins: %s Total, %s Moved, %s Failed".formatted(
                legacyCachedSkins.storedSkins.size(), skinsMigrated, skinsFailedToMigrate));
    }

    @Override
    protected SingleSkin getDefault()
    {
        return new SingleSkin();
    }

    public synchronized boolean cache(GameProfile profile)
    {
        var path = this.getPath(profile.name()) + ".json";
        var file = this.directoryStorage.getFile(path, true);
        if (file == null)
        {
            if (FeatherMorphMain.getInstance().debugOutputEnabled())
                logger.warn("[DEBUG] File is null, can't cache skin!");

            return false;
        }

        var wrapper = SingleSkin.fromProfile(profile);

        var json = gson.toJson(wrapper);
        try
        {
            FileUtils.writeStringToFile(file, json, StandardCharsets.UTF_8);
            return true;
        }
        catch (IOException e)
        {
            logger.error("Unable to write file, can't cache skin.", e);
            return false;
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
