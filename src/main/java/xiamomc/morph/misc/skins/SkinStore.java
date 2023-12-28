package xiamomc.morph.misc.skins;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.storage.MorphJsonBasedStorage;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SkinStore extends MorphJsonBasedStorage<SkinStoreRoot>
{
    @Override
    protected @NotNull String getFileName()
    {
        return "stored_skins.json";
    }

    @Override
    protected @NotNull SkinStoreRoot createDefault()
    {
        return new SkinStoreRoot();
    }

    @Override
    protected @NotNull String getDisplayName()
    {
        return "Server renderer skin store";
    }

    public synchronized void cache(GameProfile profile)
    {
        storingObject.storedSkins.add(SingleSkin.fromProfile(profile));

        saveConfiguration();
    }

    public void remove(String name)
    {
        storingObject.storedSkins.removeIf(ss -> ss.name.equalsIgnoreCase(name));
    }

    @Nullable
    public GameProfile get(String name)
    {
        var single = storingObject.storedSkins.stream().filter(ss ->
                ss.name.equalsIgnoreCase(name)).findFirst().orElse(null);

        if (single == null) return null;

        if (System.currentTimeMillis() > single.expiresAt)
        {
            this.addSchedule(() -> storingObject.storedSkins.remove(single));

            return null;
        }

        return single.toGameProfile();
    }
}
