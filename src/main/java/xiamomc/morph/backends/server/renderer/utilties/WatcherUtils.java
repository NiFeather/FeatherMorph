package xiamomc.morph.backends.server.renderer.utilties;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;
import org.bukkit.entity.Player;
import xiamomc.morph.backends.server.renderer.network.datawatcher.watchers.SingleWatcher;
import xiamomc.morph.misc.NmsRecord;

import java.util.List;

public class WatcherUtils
{
    public static CompoundTag buildCompoundFromWatcher(SingleWatcher watcher)
    {
        var tag = new CompoundTag();

        watcher.writeToCompound(tag);

        return tag;
    }

    public static List<Player> getAffectedPlayers(Player sourcePlayer)
    {
        var players = sourcePlayer.getWorld().getPlayers();
        players.remove(sourcePlayer);

        var nmsRec = NmsRecord.of(sourcePlayer);

        if (nmsRec.nmsPlayer().gameMode.getGameModeForPlayer() == GameType.SPECTATOR)
        {
            players.removeIf(bukkitPlayer ->
                    nmsRec.interactManager().getGameModeForPlayer() != GameType.SPECTATOR);
        }

        var nmsWorld = nmsRec.nmsWorld();
        var tracking = nmsWorld.spigotConfig.playerTrackingRange + 1;
        players.removeIf(p -> sourcePlayer.getLocation().distance(p.getLocation()) > tracking);

        return players;
    }
}
