package xiamomc.morph.ac;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public interface IFlyChecker
{
    void onEvent(PlayerMoveEvent e);
    void setLastLegalLocation(Player player, Location loc, boolean ignoreNextMovement);
    void dropMeta(Player player);
}
