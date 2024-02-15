package xiamomc.morph.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.skins.PlayerSkinProvider;

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
