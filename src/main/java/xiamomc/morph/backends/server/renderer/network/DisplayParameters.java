package xiamomc.morph.backends.server.renderer.network;

import com.mojang.authlib.GameProfile;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.PlayerWatcher;
import xiamomc.morph.backends.server.renderer.network.registries.CustomEntries;

public class DisplayParameters
{
    @Deprecated(since = "1.2.5", forRemoval = true)
    public EntityType getEntityType()
    {
        return singleWatcher.getEntityType();
    }

    public SingleWatcher getWatcher()
    {
        return singleWatcher;
    }

    @Nullable
    public GameProfile getProfile()
    {
        return gameProfile;
    }

    private final SingleWatcher singleWatcher;
    private final GameProfile gameProfile;
    private boolean dontRandomProfileUUID = false;
    private boolean includeMeta = true;

    public DisplayParameters setDontRandomProfileUUID()
    {
        dontRandomProfileUUID = true;

        return this;
    }

    public boolean dontRandomProfileUUID()
    {
        return dontRandomProfileUUID;
    }

    public DisplayParameters setDontIncludeMeta()
    {
        includeMeta = false;

        return this;
    }

    @Deprecated(since = "1.2.5", forRemoval = true)
    public boolean includeMeta()
    {
        return includeMeta;
    }

    public boolean includeMetaPackets()
    {
        return includeMeta;
    }

    public DisplayParameters(SingleWatcher watcher, @Nullable GameProfile profile)
    {
        this.singleWatcher = watcher;
        this.gameProfile = profile;
    }

    public static DisplayParameters fromWatcher(SingleWatcher watcher)
    {
        var profile = (watcher instanceof PlayerWatcher)
                    ? watcher.readEntry(CustomEntries.PROFILE)
                    : null;

        return new DisplayParameters(
                watcher,
                profile
        );
    }
}
