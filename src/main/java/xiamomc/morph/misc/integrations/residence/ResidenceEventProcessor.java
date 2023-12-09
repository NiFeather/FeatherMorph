package xiamomc.morph.misc.integrations.residence;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.event.ResidenceChangedEvent;
import com.bekvon.bukkit.residence.event.ResidenceFlagChangeEvent;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.abilities.impl.FlyAbility;

public class ResidenceEventProcessor extends MorphPluginObject implements Listener
{
    @EventHandler
    public void onEnterResidence(ResidenceChangedEvent e)
    {
        var player = e.getPlayer();
        var newResidence = e.getTo();

        if (newResidence == null)
        {
            FlyAbility.unBlockPlayer(player, this);
            return;
        }

        var residencePermissions = newResidence.getPermissions();
        if (residencePermissions.playerHas(player, Flags.nofly, false))
        {
            FlyAbility.blockPlayer(player, this);
        }
        else
        {
            FlyAbility.unBlockPlayer(player, this);
        }
    }

    @EventHandler
    public void onResidenceFlagChange(ResidenceFlagChangeEvent e)
    {
        var residence = e.getResidence();
        var players = residence.getPlayersInResidence();

        var flagName = e.getFlag();
        if (!flagName.equals(Flags.nofly.getName()))
            return;

        var newState = e.getNewState();
        var canFly = newState == FlagPermissions.FlagState.FALSE || newState == FlagPermissions.FlagState.NEITHER;

        players.forEach(p ->
        {
            if (!canFly)
            {
                FlyAbility.blockPlayer(p, this);
            }
            else
            {
                FlyAbility.unBlockPlayer(p, this);
            }
        });
    }
}
