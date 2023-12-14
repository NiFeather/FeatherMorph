package xiamomc.morph.backends.server.renderer.network;

import com.mojang.authlib.GameProfile;
import org.bukkit.entity.EntityType;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;

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

    public DisplayParameters(EntityType bindingType, SingleWatcher watcher, GameProfile profile)
    {
        this.entityType = bindingType;
        this.singleWatcher = watcher;
        this.gameProfile = profile;
    }
}
