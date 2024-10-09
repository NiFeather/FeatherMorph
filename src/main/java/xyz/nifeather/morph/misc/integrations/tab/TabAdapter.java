package xyz.nifeather.morph.misc.integrations.tab;

import com.google.common.io.ByteStreams;
import me.neznamy.tab.shared.TAB;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.events.api.gameplay.PlayerDisguisedFromOfflineStateEvent;
import xyz.nifeather.morph.events.api.gameplay.PlayerJoinedWithDisguiseEvent;
import xyz.nifeather.morph.events.api.gameplay.PlayerMorphEvent;
import xyz.nifeather.morph.events.api.gameplay.PlayerUnMorphEvent;
import xiamomc.pluginbase.Annotations.Initializer;

public class TabAdapter extends MorphPluginObject implements Listener
{
    public static final String bungeeCommandChannel = "morph:bcmd";

    @Initializer
    private void load()
    {
        var messenger = plugin.getServer().getMessenger();

        messenger.registerOutgoingPluginChannel(plugin, bungeeCommandChannel);
    }

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
        var data = ByteStreams.newDataOutput();
        data.writeUTF("tag_hide");
        player.sendPluginMessage(plugin, bungeeCommandChannel, data.toByteArray());

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
