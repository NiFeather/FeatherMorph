package xiamomc.morph.abilities.impl;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.MorphAbility;
import xiamomc.morph.abilities.options.FlyOption;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.storage.skill.ISkillOption;

import java.util.Map;

public class FlyAbility extends MorphAbility<FlyOption>
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

    private final Map<String, FlyOption> options = new Object2ObjectOpenHashMap<>();

    private final FlyOption option = new FlyOption();

    @Override
    public ISkillOption getOption()
    {
        return option;
    }

    @Override
    public boolean setOption(@NotNull String disguiseIdentifier, @Nullable FlyOption option)
    {
        if (option == null) return false;

        options.put(disguiseIdentifier, option);

        return true;
    }

    @Override
    public void clearOptions()
    {
        options.clear();
    }

    private float getTargetFlySpeed(String identifier)
    {
        if (identifier == null) return 0;

        var value = options.get(identifier);

        if (value != null)
            return value.getFlyingSpeed();
        else
            return 0;
    }

    public boolean updateFlyingAbility(DisguiseState state)
    {
        var player = state.getPlayer();

        player.setAllowFlight(true);

        if (player.getGameMode() != GameMode.SPECTATOR)
        {
            float speed;

            speed = getTargetFlySpeed(state.getDisguiseIdentifier());

            if (speed == 0)
                speed=  getTargetFlySpeed(state.getSkillIdentifier());

            player.setFlySpeed(speed);
        }

        return true;
    }
}
