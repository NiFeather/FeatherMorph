package xiamomc.morph.abilities.impl;

import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.MorphAbility;
import xiamomc.morph.misc.DisguiseState;

public class FlyAbility extends MorphAbility
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.CAN_FLY;
    }

    @Override
    public boolean applyToPlayer(Player player, DisguiseState state)
    {
        super.applyToPlayer(player, state);

        return updateFlyingAbility(state);
    }

    @Override
    public boolean revokeFromPlayer(Player player, DisguiseState state)
    {
        super.revokeFromPlayer(player, state);

        //取消玩家飞行
        var gamemode = player.getGameMode();

        if (gamemode != GameMode.CREATIVE && gamemode != GameMode.SPECTATOR)
            player.setAllowFlight(false);

        player.setFlySpeed(0.1f);

        return true;
    }

    private void setPlayerFlySpeed(Player player, EntityType type)
    {
        var gameMode = player.getGameMode();
        if (type == null || gameMode.equals(GameMode.SPECTATOR)) return;

        switch (type)
        {
            case ALLAY, BEE, BLAZE, VEX, BAT, PARROT -> player.setFlySpeed(0.05f);
            case GHAST, PHANTOM -> player.setFlySpeed(0.06f);
            case ENDER_DRAGON -> player.setFlySpeed(0.15f);
            default -> player.setFlySpeed(0.1f);
        }
    }

    public boolean updateFlyingAbility(DisguiseState state)
    {
        var player = state.getPlayer();

        player.setAllowFlight(true);
        setPlayerFlySpeed(player, state.getDisguise().getType().getEntityType());

        return true;
    }
}
