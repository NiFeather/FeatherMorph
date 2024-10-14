package xyz.nifeather.morph.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.misc.skins.PlayerSkinProvider;

public class PlayerSkinProcessor extends MorphPluginObject implements Listener
{
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        var skins = PlayerSkinProvider.getInstance();

        var playerProfile = e.getPlayer().getPlayerProfile();

        if (!playerProfile.hasTextures()) return;
        skins.cacheProfile(playerProfile);
    }
}
