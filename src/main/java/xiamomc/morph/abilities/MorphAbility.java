package xiamomc.morph.abilities;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.Player;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.storage.skill.ISkillOption;

import java.util.List;

public abstract class MorphAbility<T extends ISkillOption> extends MorphPluginObject implements IMorphAbility<T>
{
    protected final List<Player> appliedPlayers = new ObjectArrayList<>();

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
