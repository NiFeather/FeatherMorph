package xiamomc.morph.providers;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import net.kyori.adventure.text.Component;
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
    protected boolean canConstruct(DisguiseInfo info, Entity targetEntity, @Nullable DisguiseState theirState)
    {
        if (theirState != null)
        {
            var type = DisguiseTypes.fromId(theirState.getDisguiseIdentifier());

            return type == DisguiseTypes.PLAYER
                    && type.toStrippedId(theirState.getDisguiseIdentifier()).equals(info.playerDisguiseTargetName);
        }

        if (!(targetEntity instanceof Player targetPlayer))
            return false;

        return targetPlayer.getName().equals(info.playerDisguiseTargetName);
    }

    @Override
    protected boolean canCopyDisguise(DisguiseInfo info, Entity targetEntity,
                                      @Nullable DisguiseState theirState, @NotNull Disguise theirDisguise)
    {
        if (theirDisguise instanceof PlayerDisguise playerDisguise)
            return playerDisguise.getName().equals(info.playerDisguiseTargetName);

        return false;
    }

    @Override
    public boolean unMorph(Player player, DisguiseState state)
    {
        return super.unMorph(player, state);
    }

    @Override
    public Component getDisplayName(String disguiseIdentifier)
    {
        return Component.text(DisguiseTypes.PLAYER.toStrippedId(disguiseIdentifier));
    }
}
