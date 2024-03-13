package xiamomc.morph.misc.integrations.tab;

import me.neznamy.tab.shared.TAB;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.events.api.gameplay.PlayerDisguisedFromOfflineStateEvent;
import xiamomc.morph.events.api.gameplay.PlayerJoinedWithDisguiseEvent;
import xiamomc.morph.events.api.gameplay.PlayerMorphEvent;
import xiamomc.morph.events.api.gameplay.PlayerUnMorphEvent;

public class TabAdapter extends MorphPluginObject implements Listener
{
    @EventHandler(ignoreCancelled = true)
    public void onMorph(PlayerMorphEvent e)
    {
        this.hideNameTag(e.getPlayer());
    }

    @EventHandler
    public void onOfflineState(PlayerDisguisedFromOfflineStateEvent e)
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

    private void hideNameTag(Player player)
    {
        var tabInstance = TAB.getInstance();

        var tagManager = tabInstance.getNameTagManager();
        if (tagManager == null) return;

        var tabPlayer = tabInstance.getPlayer(player.getUniqueId());
        if (tabPlayer == null) return;

        tabInstance.getNameTagManager().hideNameTag(tabPlayer);
    }

    private void showNameTag(Player player)
    {
        var tabInstance = TAB.getInstance();

        var tagManager = tabInstance.getNameTagManager();
        if (tagManager == null) return;

        var tabPlayer = tabInstance.getPlayer(player.getUniqueId());
        if (tabPlayer == null) return;

        tabInstance.getNameTagManager().showNameTag(tabPlayer);
    }
}
