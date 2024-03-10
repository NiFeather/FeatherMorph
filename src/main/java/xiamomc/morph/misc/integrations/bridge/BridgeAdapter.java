package xiamomc.morph.misc.integrations.bridge;

import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.neznamy.tab.bridge.bukkit.BukkitBridge;
import me.neznamy.tab.bridge.bukkit.BukkitBridgePlayer;
import me.neznamy.tab.bridge.bukkit.features.unlimitedtags.BridgeNameTagX;
import me.neznamy.tab.bridge.shared.BridgePlayer;
import me.neznamy.tab.bridge.shared.TABBridge;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.events.api.gameplay.PlayerJoinedWithDisguiseEvent;
import xiamomc.morph.events.api.gameplay.PlayerMorphEvent;
import xiamomc.morph.events.api.gameplay.PlayerUnMorphEvent;
import xiamomc.pluginbase.Annotations.Initializer;

import java.util.List;

public class BridgeAdapter extends MorphPluginObject implements Listener
{
    @Initializer
    private void load()
    {
    }

    @EventHandler(ignoreCancelled = true)
    public void onMorph(PlayerMorphEvent e)
    {
        this.hideNameTag(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoinWithDisguise(PlayerJoinedWithDisguiseEvent e)
    {
        this.hideNameTag(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onUnmorph(PlayerUnMorphEvent e)
    {
        showNameTag(e.getPlayer());
    }

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent e)
    {
        hideNameTag(e.getPlayer());
    }

    private final List<BridgePlayer> handledPlayers = new ObjectArrayList<>();

    private void hideNameTag(Player player)
    {
        var bridgeInstance = TABBridge.getInstance();

        var bridgePlayer = bridgeInstance.getPlayer(player.getUniqueId());
        if (bridgePlayer == null) return;

        bridgePlayer.setInvisible(true);
        bridgeInstance.removePlayer(bridgePlayer);
        handledPlayers.add(bridgePlayer);
    }

    private void showNameTag(Player player)
    {
        var bridgeInstance = TABBridge.getInstance();

        var bridgePlayer = handledPlayers.stream()
                .filter(bp -> bp.getUniqueId().equals(player.getUniqueId()))
                .findFirst()
                .orElse(null);

        if (bridgePlayer == null) return;

        bridgeInstance.addPlayer(bridgePlayer);
    }
}
