package xyz.nifeather.morph.backends.server.renderer.network;

import com.mojang.authlib.GameProfile;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;

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
    @Deprecated(since = "1.2.6", forRemoval = true)
    public GameProfile getProfile()
    {
        return singleWatcher.readEntry(CustomEntries.PROFILE);
    }

    private final SingleWatcher singleWatcher;
    private boolean includeMeta = true;

    @Deprecated(since = "1.2.6", forRemoval = true)
    public DisplayParameters setDontRandomProfileUUID()
    {
        return this;
    }

    @Deprecated(since = "1.2.6", forRemoval = true)
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

    @Deprecated(since = "1.2.6", forRemoval = true)
    public boolean includeMetaPackets()
    {
        return includeMeta;
    }

    public DisplayParameters(SingleWatcher watcher)
    {
        this.singleWatcher = watcher;
    }

    @Deprecated(since = "1.2.6", forRemoval = true)
    public DisplayParameters(SingleWatcher watcher, @Nullable GameProfile profile)
    {
        this(watcher);
    }

    public static DisplayParameters fromWatcher(SingleWatcher watcher)
    {
        return new DisplayParameters(watcher);
    }
}
