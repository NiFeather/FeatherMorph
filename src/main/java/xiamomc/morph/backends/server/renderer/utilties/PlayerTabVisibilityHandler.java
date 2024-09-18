package xiamomc.morph.backends.server.renderer.utilties;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.NmsRecord;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerTabVisibilityHandler extends MorphPluginObject
{
    private final static AtomicReference<PlayerTabVisibilityHandler> instance = new AtomicReference<>(null);
    public static PlayerTabVisibilityHandler instance()
    {
        if (instance.get() == null)
            instance.set(new PlayerTabVisibilityHandler());

        return instance.get();
    }

    private final List<UUID> hiddenPlayers = new ObjectArrayList<>();

    public void hidePlayer(UUID uuid)
    {
        if (hiddenPlayers.contains(uuid)) return;

        hiddenPlayers.add(uuid);

        var packet = new ClientboundPlayerInfoRemovePacket(List.of(uuid));
        for (Player onlinePlayer : Bukkit.getOnlinePlayers())
            this.sendPacket(onlinePlayer, packet);
    }

    public void hidePlayer(@Nullable Player player)
    {
        if (player == null) return;

        this.hidePlayer(player.getUniqueId());
    }

    public void showPlayer(@Nullable Player player)
    {
        if (player == null) return;

        this.showPlayer(player.getUniqueId());
    }

    private void sendPacket(Player player, Packet<?> packet)
    {
        var nmsPlayer = NmsRecord.ofPlayer(player);

        // Yes this can be NULL
        if (nmsPlayer.connection == null) return;

        nmsPlayer.connection.sendPacket(packet);
    }

    public void showPlayer(UUID uuid)
    {
        hiddenPlayers.remove(uuid);

        var player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;

        var nmsHandle = NmsRecord.ofPlayer(player);

        var packet = new ClientboundPlayerInfoUpdatePacket(
                EnumSet.allOf(ClientboundPlayerInfoUpdatePacket.Action.class),
                Set.of(nmsHandle)
        );

        for (Player onlinePlayer : Bukkit.getOnlinePlayers())
            this.sendPacket(onlinePlayer, packet);
    }

    /**
     * 根据列表向给定的玩家隐藏一些人
     * @param player 目标玩家
     */
    public void handle(Player player)
    {
        var packet = new ClientboundPlayerInfoRemovePacket(this.hiddenPlayers);
        this.sendPacket(player, packet);
    }
}
