package xyz.nifeather.morph.backends.server.renderer.utilties;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.backends.server.renderer.network.datawatcher.watchers.VirtualEntity;
import xyz.nifeather.morph.misc.NmsRecord;

import java.util.List;
import java.util.function.Function;

public class WatcherUtils
{
    public static CompoundTag buildCompoundFromWatcher(VirtualEntity watcher)
    {
        var tag = new CompoundTag();

        watcher.writeToCompound(tag);

        return tag;
    }

    /**
     *
     * @param location
     * @param filter Return {@code true} if the player should be filtered out.
     * @return
     */
    public static List<Player> getAffectedPlayers(Location location, Function<Player, Boolean> filter)
    {
        var players = location.getWorld().getPlayers();
        var playerTrackingRange = ((CraftWorld)location.getWorld()).getHandle().spigotConfig.playerTrackingRange + 1;

        players.removeIf(p -> location.distance(p.getLocation()) > playerTrackingRange || filter.apply(p));

        return players;
    }

    public static List<Player> getAffectedPlayers(@NotNull Player sourcePlayer)
    {
        var isSpectator = NmsRecord.of(sourcePlayer).nmsPlayer().gameMode.getGameModeForPlayer() == GameType.SPECTATOR;

        return getAffectedPlayers(sourcePlayer.getLocation(), p ->
        {
            return sourcePlayer.equals(p) || isSpectator ? NmsRecord.ofPlayer(p).gameMode.getGameModeForPlayer() != GameType.SPECTATOR : false;
        });
    }
}
