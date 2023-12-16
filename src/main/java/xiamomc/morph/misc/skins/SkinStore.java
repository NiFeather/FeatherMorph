package xiamomc.morph.misc.skins;

import org.jetbrains.annotations.NotNull;
import xiamomc.morph.storage.MorphJsonBasedStorage;

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
/*
    @Nullable
    public GameProfile getProfile(String skinName)
    {
        var matchedSkin = storingObject.storedSkins.stream()
                .filter(skin -> skin.name.equalsIgnoreCase(skinName))
                .findFirst().orElse(null);

        if (matchedSkin == null) return null;

        var profile = new GameProfile(matchedSkin.uuid, matchedSkin.name);
        profile.getProperties().put("textures", new Property())
    }

 */
}
