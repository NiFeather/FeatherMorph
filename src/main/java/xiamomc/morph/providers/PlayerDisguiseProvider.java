package xiamomc.morph.providers;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.misc.DisguiseInfo;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.DisguiseTypes;

public class PlayerDisguiseProvider extends DefaultDisguiseProvider
{
    @Override
    public @NotNull String getIdentifier()
    {
        return DisguiseTypes.PLAYER.getNameSpace();
    }

    @Override
    public @NotNull DisguiseResult morph(Player player, DisguiseInfo disguiseInfo, @Nullable Entity targetEntity)
    {
        var id = disguiseInfo.getIdentifier();

        if (DisguiseTypes.fromId(id) != DisguiseTypes.PLAYER)
            return DisguiseResult.fail();

        var result = getCopy(disguiseInfo, targetEntity);
        var disguise = result.success() ? result.disguise() : new PlayerDisguise(disguiseInfo.playerDisguiseTargetName);

        DisguiseAPI.disguiseEntity(player, disguise);

        return DisguiseResult.success(disguise, result.isCopy());
    }

    @Override
    public boolean unMorph(Player player, DisguiseState state)
    {
        return super.unMorph(player, state);
    }
}
