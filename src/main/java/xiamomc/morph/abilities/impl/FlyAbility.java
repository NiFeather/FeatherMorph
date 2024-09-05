package xiamomc.morph.abilities.impl;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.GameEvent;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.MorphAbility;
import xiamomc.morph.abilities.options.FlyOption;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.permissions.CommonPermissions;
import xiamomc.morph.utilities.MathUtils;
import xiamomc.morph.utilities.PermissionUtils;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;
import xiamomc.pluginbase.Bindables.BindableList;

import java.util.Map;
import java.util.Stack;

public class FlyAbility extends MorphAbility<FlyOption>
{
    public FlyAbility()
    {
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.CAN_FLY;
    }

    @Override
    public boolean applyToPlayer(Player player, DisguiseState state)
    {
        if (super.applyToPlayer(player, state))
        {
            return updateFlyingState(state);
        }
        else
            return false;
    }

    private final BindableList<String> noFlyWorlds = new BindableList<>();
    private final BindableList<String> noFlyInLavaWorlds = new BindableList<>();
    private final BindableList<String> noFlyInWaterWorlds = new BindableList<>();

    @Initializer
    private void load(MorphConfigManager configManager)
    {
        configManager.getBindable(Double.class, ConfigOption.FLYABILITY_EXHAUSTION_BASE).onValueChanged((o, n) ->
        {
            var scale = ((double)ConfigOption.FLYABILITY_EXHAUSTION_BASE.defaultValue / n);
            this.exhaustionScaled = exhaustionBase * scale;
        }, true);

        configManager.bind(String.class, noFlyWorlds, ConfigOption.NOFLY_WORLDS);

        configManager.getBindable(Boolean.class, ConfigOption.FLYABILITY_IDLE_CONSUME).onValueChanged((o, n) ->
                idleConsumption = n ? 0.1D : 0D, true);

        configManager.bind(allowFlight, ConfigOption.ALLOW_FLIGHT);
        configManager.bind(String.class, noFlyInLavaWorlds, ConfigOption.FLYABILITY_DISALLOW_FLY_IN_LAVA);
        configManager.bind(String.class, noFlyInWaterWorlds, ConfigOption.FLYABILITY_DISALLOW_FLY_IN_WATER);
    }

    private final Bindable<Boolean> allowFlight = new Bindable<>(true);

    private final float exhaustionBase = 0.005f;
    private double idleConsumption = 0.25F * 0.2;
    private double exhaustionScaled = 0.005d;

    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        if (plugin.getCurrentTick() % 2 != 0) return true;

        var nmsPlayer = ((CraftPlayer) player).getHandle();

        var gameMode = nmsPlayer.gameMode.getGameModeForPlayer();
        if (gameMode == GameType.CREATIVE || gameMode == GameType.SPECTATOR)
            return super.handle(player, state);

        var option = optionMap.get(state.skillLookupIdentifier());

        var worldName = player.getWorld().getName();
        var allowFlightConditions = player.getFoodLevel() > option.getMinimumHunger()
                    && !noFlyWorlds.contains(worldName)
                    && !playerBlocked(player)
                    && playerHasCommonFlyPerm(player)
                    && (!noFlyInLavaWorlds.contains(worldName) || !player.isInLava())
                    && (!noFlyInWaterWorlds.contains(worldName) || !player.isInWaterOrBubbleColumn());

        var allowFlight = this.allowFlight.get() && (allowFlightConditions || player.hasPermission(CommonPermissions.ALWAYS_CAN_FLY));

        if (player.isFlying())
        {
            float exhaustion;

            double delta;

            // 检查玩家飞行速度是否正确
            if (plugin.getCurrentTick() % 10 == 0)
            {
                var configSpeed = option.getFlyingSpeed();
                if (player.getFlySpeed() != configSpeed && nmsPlayer.gameMode.isSurvival())
                    player.setFlySpeed(configSpeed);
            }

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

            nmsPlayer.getFoodData().addExhaustion(exhaustion);

            if (player.getTicksLived() % 5 == 0)
                player.getWorld().sendGameEvent(player, GameEvent.FLAP, player.getLocation().toVector());

            if (!allowFlight)
                player.setFlying(false);
        }

        var playerCanFly = nmsPlayer.getAbilities().mayfly;
        if (playerCanFly != allowFlight)
            player.setAllowFlight(allowFlight);

        return super.handle(player, state);
    }

    private boolean playerHasCommonFlyPerm(Player player)
    {
        var worldPerm = CommonPermissions.CanFlyIn(player.getWorld().getName());

        return player.hasPermission(CommonPermissions.CAN_FLY)
                && PermissionUtils.hasPermission(player, worldPerm, true);
    }

    private float handleMovementForSpeed(double movementDelta)
    {
        var movementBase = 0.25f;// * config.getHungerConsumeMultiplier();
        var movementMultiplier = (float)movementDelta / movementBase; //(5.1f * config.getFlyingSpeed());

        return (float)exhaustionScaled * movementMultiplier;
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

    public float getTargetFlySpeed(String identifier)
    {
        if (identifier == null) return Float.NaN;

        var value = optionMap.getOrDefault(identifier, null);

        if (value != null)
            return value.getFlyingSpeed();
        else
            return Float.NaN;
    }

    public boolean updateFlyingState(DisguiseState state)
    {
        var player = state.getPlayer();

        player.setAllowFlight(true);

        if (player.getGameMode() != GameMode.SPECTATOR)
        {
            float speed = getTargetFlySpeed(state.skillLookupIdentifier());

            speed = Float.isNaN(speed) ? 0.1f : MathUtils.clamp(-1f, 1f, speed);

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
        if (!this.isPlayerApplied(player)) return;

        var state = manager.getDisguiseStateFor(player);

        if (state != null)
        {
            var flying = player.isFlying();

            //立即更新状态不会生效，需要延迟1tick再进行
            this.addSchedule(() ->
            {
                if (isPlayerApplied(player))
                {
                    this.updateFlyingState(state);

                    if (flying)
                        player.setFlying(true);
                }
            });
        }
        else
        {
            logger.warn(player.getName() + " have fly ability applied, but its DisguiseState is null? Revoking...");
            this.revokeFromPlayer(player, null);
        }
    }

    private static final Map<Player, Stack<Object>> blockedPlayersMap = new Object2ObjectOpenHashMap<>();

    public static boolean playerBlocked(Player player)
    {
        var stack = blockedPlayersMap.getOrDefault(player, null);
        return stack != null && !stack.isEmpty();
    }

    public static void blockPlayer(Player player, Object requestSource)
    {
        var stack = blockedPlayersMap.getOrDefault(player, null);
        if (stack == null)
        {
            stack = new Stack<>();
            blockedPlayersMap.put(player, stack);
        }

        if (!stack.contains(requestSource))
            stack.push(requestSource);
    }

    public static void unBlockPlayer(Player player, Object requestSource)
    {
        var stack = blockedPlayersMap.getOrDefault(player, new Stack<>());
        stack.remove(requestSource);
    }
}
