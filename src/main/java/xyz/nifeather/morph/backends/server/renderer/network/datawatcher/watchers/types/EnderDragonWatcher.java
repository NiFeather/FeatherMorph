package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntries;
import xyz.nifeather.morph.backends.server.renderer.network.registries.CustomEntry;

public class EnderDragonWatcher extends LivingEntityWatcher
{
    public EnderDragonWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.ENDER_DRAGON);
    }

    @Override
    public <X> @Nullable X readEntry(CustomEntry<X> entry)
    {
        if (entry == CustomEntries.OVERLAYED_YAW)
            return (X) Float.valueOf(180f + getBindingPlayer().getYaw());

        return super.readEntry(entry);
    }
}
