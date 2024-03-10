package xiamomc.morph.misc.integrations.bridge;

import com.comphenix.protocol.ProtocolLibrary;
import me.neznamy.tab.bridge.bukkit.BukkitBridgePlayer;
import me.neznamy.tab.bridge.shared.BridgePlayer;
import me.neznamy.tab.bridge.shared.TABBridge;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.events.api.gameplay.PlayerJoinedWithDisguiseEvent;
import xiamomc.morph.events.api.gameplay.PlayerMorphEvent;
import xiamomc.morph.events.api.gameplay.PlayerUnMorphEvent;
import xiamomc.pluginbase.Annotations.Initializer;

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

    private void hideNameTag(Player player)
    {
        var bridgeInstance = TABBridge.getInstance();

        var bridgePlayer = bridgeInstance.getPlayer(player.getUniqueId());
        if (bridgePlayer == null) return;

        bridgePlayer.setInvisible(true);
        bridgeInstance.removePlayer(bridgePlayer);
    }

    @EventHandler(ignoreCancelled = true)
    public void onUnmorph(PlayerUnMorphEvent e)
    {
        var bridgeInstance = TABBridge.getInstance();

        bridgeInstance.addPlayer(new BukkitBridgePlayer(e.getPlayer(), ProtocolLibrary.getProtocolManager().getProtocolVersion(e.getPlayer())));
        //var bridgePlayer = bridgeInstance.getPlayer(e.getPlayer().getUniqueId());

        //if (bridgePlayer == null) return;

        //bridgePlayer.setInvisible(false);
        //bridgePlayer.setVanished(false);
    }
}
