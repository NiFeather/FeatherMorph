package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;
import xiamomc.morph.backends.server.renderer.network.registries.EntryIndex;
import xiamomc.morph.backends.server.renderer.network.registries.RegistryKey;
import xiamomc.pluginbase.Annotations.Resolved;

public class ArmorStandWatcher extends InventoryLivingWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.ARMOR_STAND);
    }

    public ArmorStandWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.ARMOR_STAND);
    }

    @Override
    protected void doSync()
    {
        super.doSync();
    }
}
