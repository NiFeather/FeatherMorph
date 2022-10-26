package xiamomc.morph.abilities;

import org.bukkit.entity.Player;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.DisguiseState;

import java.util.ArrayList;
import java.util.List;

public abstract class MorphAbility extends MorphPluginObject implements IMorphAbility
{
    protected final List<Player> appliedPlayers = new ArrayList<>();

    @Override
    public boolean applyToPlayer(Player player, DisguiseState state)
    {
        appliedPlayers.add(player);
        return true;
    }

    @Override
    public boolean revokeFromPlayer(Player player, DisguiseState state)
    {
        appliedPlayers.remove(player);
        return true;
    }

    @Override
    public List<Player> getAppliedPlayers()
    {
        return appliedPlayers;
    }
}
