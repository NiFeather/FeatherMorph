package xiamomc.morph.abilities.impl;

import net.minecraft.world.phys.Vec3;
import org.bukkit.GameEvent;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.checkerframework.checker.units.qual.degrees;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3f;

import com.google.gson.ExclusionStrategy;

import xiamomc.morph.MorphManager;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.MorphAbility;
import xiamomc.morph.abilities.options.FlyOption;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;

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
        if (super.applyToPlayer(player, state))
            return updateFlyingAbility(state);
        else
            return false;
    }

    @Initializer
    private void load(MorphConfigManager configManager)
    {
        configManager.getBindable(Double.class, ConfigOption.FLYABILITY_EXHAUSTION_BASE).onValueChanged((o, n) ->
        {
            var scale = ((double)ConfigOption.FLYABILITY_EXHAUSTION_BASE.defaultValue / n);
            this.exhaustionScaled = exhaustionBase * scale;
        }, true);

        configManager.getBindable(Boolean.class, ConfigOption.FLYABILITY_IDLE_CONSUME).onValueChanged((o, n) ->
                idleConsumption = n ? 0.1D : 0D, true);
    }

    private final float exhaustionBase = 0.005f;
    private double idleConsumption = 0.25F * 0.2;
    private double exhaustionScaled = 0.005d;

    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        var gameMode = player.getGameMode();
        if (gameMode != GameMode.CREATIVE && gameMode != GameMode.SPECTATOR)
        {
            var nmsPlayer = ((CraftPlayer) player).getHandle();
            var config = options.get(state.getSkillLookupIdentifier());

            var data = nmsPlayer.getFoodData();
            var allowFlight = data.foodLevel > config.getMinimumHunger();

            if (player.isFlying())
            {
                float exhaustion;

                double delta;

                // 当玩家骑乘实体时不要计算位移
                if (player.getVehicle() == null)
                {
                    var old = new Vec3(nmsPlayer.xOld, nmsPlayer.yOld, nmsPlayer.zOld);
                    var cur = nmsPlayer.position();
                    delta = Math.max(idleConsumption, cur.distanceTo(old));
                }
                else
                    delta = 0;

                exhaustion = handleMovementForSpeed(delta);

                data.addExhaustion(exhaustion);

                if (player.getTicksLived() % 5 == 0)
                    player.getWorld().sendGameEvent(player, GameEvent.FLAP, player.getLocation().toVector());

                if (!allowFlight)
                    player.setFlying(false);
            }

            var playerCanFly = nmsPlayer.getAbilities().mayfly;
            if (playerCanFly != allowFlight)
                player.setAllowFlight(allowFlight);
        }

        return super.handle(player, state);
    }

    private float handleMovementForSpeed(double movementDelta)
    {
        var movementBase = 0.25f;// * config.getHungerConsumeMultiplier();
        var movementMultiplier = (float)movementDelta / movementBase; //(5.1f * config.getFlyingSpeed());

        return (float)exhaustionScaled * movementMultiplier;
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent e)
    {
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

    @Override
    protected @NotNull FlyOption createOption()
    {
        return new FlyOption();
    }

    private float getTargetFlySpeed(String identifier)
    {
        if (identifier == null) return Float.NaN;

        var value = options.get(identifier);

        if (value != null)
            return value.getFlyingSpeed();
        else
            return Float.NaN;
    }

    public boolean updateFlyingAbility(DisguiseState state)
    {
        var player = state.getPlayer();

        player.setAllowFlight(true);

        if (player.getGameMode() != GameMode.SPECTATOR)
        {
            float speed = getTargetFlySpeed(state.getSkillLookupIdentifier());

            speed = Float.isNaN(speed) ? 0.1f : speed;

            if (speed > 1f) speed = 1;
            else if (speed < -1f) speed = -1;

            player.setFlySpeed(speed);
        }

        return true;
    }

    @Resolved
    private MorphManager manager;

    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent e)
    {
        var player = e.getPlayer();
        if (!this.appliedPlayers.contains(player)) return;

        var state = manager.getDisguiseStateFor(player);

        if (state != null)
        {
            var flying = player.isFlying();

            //立即更新状态不会生效，需要延迟1tick再进行
            this.addSchedule(() ->
            {
                if (appliedPlayers.contains(player))
                {
                    this.updateFlyingAbility(state);

                    if (flying)
                        player.setFlying(true);
                }
            });
        }
        else
        {
            logger.warn(player.getName() + " have fly ability applied, but its DisguiseState is null?");
            this.appliedPlayers.remove(player);
        }
    }
}
