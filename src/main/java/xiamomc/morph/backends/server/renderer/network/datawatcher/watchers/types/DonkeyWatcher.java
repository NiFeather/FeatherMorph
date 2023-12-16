package xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.types.horses.ChestedHorseWatcher;

public class DonkeyWatcher extends ChestedHorseWatcher
{
    public DonkeyWatcher(Player bindingPlayer)
    {
        super(bindingPlayer, EntityType.DONKEY);
    }
}
