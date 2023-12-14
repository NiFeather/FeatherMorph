package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.PacketFactory;
import xiamomc.morph.backends.server.renderer.network.datawatcher.ValueIndex;
import xiamomc.pluginbase.Annotations.Resolved;

public class GhastWatcher extends LivingEntityWatcher
{
    @Override
    protected void initRegistry()
    {
        super.initRegistry();

        register(ValueIndex.GHAST);
    }

    public GhastWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.GHAST);
    }
}
