package xiamomc.morph.backends.server.renderer.network;

import com.mojang.authlib.GameProfile;
import org.bukkit.entity.EntityType;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.PlayerWatcher;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;

public class DisplayParameters
{
    public EntityType getEntityType()
    {
        return entityType;
    }

    public SingleWatcher getWatcher()
    {
        return singleWatcher;
    }

    public GameProfile getProfile()
    {
        return gameProfile;
    }

    private final EntityType entityType;
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

    public boolean includeMeta()
    {
        return includeMeta;
    }

    public DisplayParameters(EntityType bindingType, SingleWatcher watcher, GameProfile profile)
    {
        this.entityType = bindingType;
        this.singleWatcher = watcher;
        this.gameProfile = profile;
    }

    public static DisplayParameters fromWatcher(SingleWatcher watcher)
    {
        var profile = (watcher instanceof PlayerWatcher)
                    ? watcher.get(EntryIndex.PROFILE)
                    : null;

        return new DisplayParameters(
                watcher.getEntityType(),
                watcher,
                profile
        );
    }
}
