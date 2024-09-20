package xiamomc.morph.misc;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.world.level.GameType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphPluginObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerTabHandler extends MorphPluginObject
{
    private final static AtomicReference<PlayerTabHandler> instance = new AtomicReference<>(null);
    public static PlayerTabHandler instance()
    {
        if (instance.get() == null)
            instance.set(new PlayerTabHandler());

        return instance.get();
    }

    private final List<UUID> hiddenPlayers = new ObjectArrayList<>();

    private void sendToAllPlayers(Packet<?> packet)
    {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers())
            this.sendPacket(onlinePlayer, packet);
    }

    private void sendPacket(Player player, Packet<?> packet)
    {
        var nmsPlayer = NmsRecord.ofPlayer(player);

        // Yes this can be NULL
        if (nmsPlayer.connection == null) return;

        nmsPlayer.connection.sendPacket(packet);
    }

    //region Hide/Show Player

    public void hidePlayer(UUID uuid)
    {
        if (hiddenPlayers.contains(uuid)) return;

        hiddenPlayers.add(uuid);

        var packet = new ClientboundPlayerInfoRemovePacket(List.of(uuid));
        for (Player onlinePlayer : Bukkit.getOnlinePlayers())
        {
            if (onlinePlayer.getUniqueId().equals(uuid)) continue;

            this.sendPacket(onlinePlayer, packet);
        }
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

        this.sendToAllPlayers(packet);
    }

    //endregion Hide/Show Player

    //region Fake players

    private final Map<UUID, GameProfile> fakePlayers = new ConcurrentHashMap<>();

    public void showFakePlayer(UUID disguiseUUID, GameProfile profile)
    {
        if (fakePlayers.containsKey(disguiseUUID))
            this.hideFakePlayer(disguiseUUID);

        fakePlayers.put(disguiseUUID, profile);

        var infoPacket = new ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(
                        ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED
                ),
                new ClientboundPlayerInfoUpdatePacket.Entry(
                        disguiseUUID, profile,
                        true,
                        114514, GameType.DEFAULT_MODE,
                        null, null
                )
        );

        this.sendToAllPlayers(infoPacket);
    }

    public void hideFakePlayer(UUID disguiseUUID)
    {
        fakePlayers.remove(disguiseUUID);

        var packet = new ClientboundPlayerInfoRemovePacket(List.of(disguiseUUID));
        this.sendToAllPlayers(packet);
    }

    //endregion Fake players

    /**
     * 根据列表向给定的玩家隐藏一些人
     * @param player 目标玩家
     */
    public void handle(Player player)
    {
        var list = new ObjectArrayList<>(this.hiddenPlayers);
        list.removeIf(uuid -> uuid.equals(player.getUniqueId()));

        var packet = new ClientboundPlayerInfoRemovePacket(list);
        this.sendPacket(player, packet);

        this.fakePlayers.forEach((disguiseUUID, profile) ->
        {
            var infoPacket = new ClientboundPlayerInfoUpdatePacket(
                    EnumSet.of(
                            ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
                            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED
                    ),
                    new ClientboundPlayerInfoUpdatePacket.Entry(
                            disguiseUUID, profile,
                            true,
                            114514, GameType.DEFAULT_MODE,
                            null, null
                    )
            );

            this.sendPacket(player, infoPacket);
        });
    }
}
