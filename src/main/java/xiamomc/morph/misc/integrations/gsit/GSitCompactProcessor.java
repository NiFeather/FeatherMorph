package xiamomc.morph.misc.integrations.gsit;

import dev.geco.gsit.api.event.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.List;

public class GSitCompactProcessor extends MorphPluginObject implements Listener
{
    @Resolved
    private MorphManager morphs;

    //region GSit <-> LibsDisguises workaround

    @EventHandler
    public void onLeave(PlayerQuitEvent e)
    {
        gSitHandlingPlayers.remove(e.getPlayer());
    }

    @EventHandler
    public void onEntityGetUp(EntityGetUpSitEvent e)
    {
        if (e.getEntity() instanceof Player player)
            showDisguiseFor(player);
    }

    private final List<Player> gSitHandlingPlayers = new ObjectArrayList<>();

    @EventHandler
    public void onEntitySit(EntitySitEvent e)
    {
        if (e.getEntity() instanceof Player player)
            hideDisguiseFor(player);
    }

    @EventHandler
    public void onEarlyPlayerPlayerSit(PrePlayerPlayerSitEvent e)
    {
        var state = morphs.getDisguiseStateFor(e.getTarget());

        if (state != null && !state.getDisguise().isPlayerDisguise())
        {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerPlayerSit(PlayerPlayerSitEvent e)
    {
        gSitHandlingPlayers.add(e.getPlayer());
        hideDisguiseFor(e.getPlayer());
    }

    @EventHandler
    public void onPlayerGetUpPlayerSit(PlayerGetUpPlayerSitEvent e)
    {
        if (gSitHandlingPlayers.contains(e.getPlayer()))
        {
            showDisguiseFor(e.getPlayer());
            gSitHandlingPlayers.remove(e.getPlayer());
        }
    }

    private void hideDisguiseFor(Player player)
    {
        if (DisguiseAPI.isDisguised(player))
            DisguiseUtilities.removeSelfDisguise(DisguiseAPI.getDisguise(player));
    }

    private void showDisguiseFor(Player player)
    {
        if (DisguiseAPI.isDisguised(player))
            this.addSchedule(c ->
            {
                if (DisguiseAPI.isDisguised(player))
                    DisguiseUtilities.setupFakeDisguise(DisguiseAPI.getDisguise(player));
            });
    }

    //endregion  GSit <-> LibsDisguises workaround
}
