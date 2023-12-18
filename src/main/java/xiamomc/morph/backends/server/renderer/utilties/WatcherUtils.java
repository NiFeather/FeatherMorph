package xiamomc.morph.backends.server.renderer.utilties;

import net.minecraft.nbt.CompoundTag;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;

public class WatcherUtils
{
    public static CompoundTag buildCompoundFromWatcher(SingleWatcher watcher)
    {
        var tag = new CompoundTag();

        watcher.writeToCompound(tag);

        return tag;
    }
}
