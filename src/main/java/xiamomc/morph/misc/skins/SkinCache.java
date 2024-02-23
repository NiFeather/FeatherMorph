package xiamomc.morph.misc.skins;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.storage.MorphJsonBasedStorage;

import java.util.List;
import java.util.Optional;

public class SkinCache extends MorphJsonBasedStorage<SkinCacheRoot>
{
    @Override
    protected @NotNull String getFileName()
    {
        return "stored_skins.json";
    }

    @Override
    protected @NotNull SkinCacheRoot createDefault()
    {
        return new SkinCacheRoot();
    }

    @Override
    protected @NotNull String getDisplayName()
    {
        return "Server renderer skin store";
    }

    public synchronized void cache(GameProfile profile)
    {
        drop(profile.getName());
        storingObject.storedSkins.add(SingleSkin.fromProfile(profile));

        saveConfiguration();
    }

    public synchronized void drop(String name)
    {
        storingObject.storedSkins.removeIf(ss -> ss.name.equalsIgnoreCase(name));

        saveConfiguration();
    }

    public synchronized void drop(GameProfile profile)
    {
        drop(profile.getName());
    }

    public synchronized void drop(SingleSkin singleSkin)
    {
        storingObject.storedSkins.remove(singleSkin);

        saveConfiguration();
    }

    public synchronized void dropAll()
    {
        storingObject.storedSkins.clear();

        saveConfiguration();
    }

    public record SkinRecord(Optional<GameProfile> profileOptional, boolean expired)
    {
    }

    @Nullable
    SingleSkin getRaw(String name)
    {
        return storingObject.storedSkins.stream().filter(ss ->
                ss.name.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @NotNull
    public SkinRecord get(String name)
    {
        var single = getRaw(name);

        if (single == null) return new SkinRecord(Optional.empty(), true);

        var profile = single.generateGameProfile();
        return new SkinRecord(
                (profile == null ? Optional.empty() : Optional.of(profile)),
                System.currentTimeMillis() > single.expiresAt);
    }

    //region Utilities

    public List<SingleSkin> listAll()
    {
        return new ObjectArrayList<>(storingObject.storedSkins);
    }

    //endregion
}
