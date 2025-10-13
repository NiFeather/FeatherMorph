package xyz.nifeather.morph.misc.integrations.residence;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.event.ResidenceChangedEvent;
import com.bekvon.bukkit.residence.event.ResidenceFlagChangeEvent;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.abilities.impl.FlyAbility;
import xyz.nifeather.morph.abilities.impl.SnowyAbility;
import xyz.nifeather.morph.api.events.gameplay.PlayerExecuteSkillEvent;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.MorphStrings;

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
            SnowyAbility.unBlockPlayer(player, this);
            return;
        }

        var residencePermissions = newResidence.getPermissions();
        if (residencePermissions.playerHas(player, Flags.nofly, false))
            FlyAbility.blockPlayer(player, this);
        else
            FlyAbility.unBlockPlayer(player, this);

        if (residencePermissions.playerHas(player, Flags.place, false))
            SnowyAbility.unBlockPlayer(player, this);
        else
            SnowyAbility.blockPlayer(player, this);
    }

    @EventHandler
    public void onResidenceFlagChange(ResidenceFlagChangeEvent e)
    {
        var residence = e.getResidence();
        var players = residence.getPlayersInResidence();

        var flagName = e.getFlag();
        var newState = e.getNewState();

        if (flagName.equals(Flags.place.getName()))
        {
            var allowPlace = newState == FlagPermissions.FlagState.TRUE;

            players.forEach(p ->
            {
                if (!allowPlace)
                    SnowyAbility.blockPlayer(p, this);
                else
                    SnowyAbility.unBlockPlayer(p, this);
            });
        }
        else if (flagName.equals(Flags.nofly.getName()))
        {
            var canFly = newState == FlagPermissions.FlagState.FALSE || newState == FlagPermissions.FlagState.NEITHER;

            players.forEach(p ->
            {
                if (!canFly)
                    FlyAbility.blockPlayer(p, this);
                else
                    FlyAbility.unBlockPlayer(p, this);
            });
        }
    }

    @EventHandler
    public void onPlayerUseSkill(PlayerExecuteSkillEvent e)
    {
        var player = e.getPlayer();
        var residence = Residence.getInstance()
                .getResidenceManager()
                .getByLoc(player);

        if (residence == null)
            return;

        var residencePermissions = residence.getPermissions();
        if (!residencePermissions.playerHas(player, Flags.use, false))
        {
            e.setCancelled(true);
            MessageUtils.send(player, MorphStrings.regionBlockedSkillString());
        }
    }
}
