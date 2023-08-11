package xiamomc.morph.ac;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.abilities.impl.FlyAbility;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.transforms.Recorder;
import xiamomc.morph.transforms.TransformUtils;
import xiamomc.morph.transforms.Transformer;
import xiamomc.morph.transforms.easings.Easing;
import xiamomc.morph.transforms.easings.impl.EasingImpl;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.List;
import java.util.Map;

public class FlyChecker extends MorphPluginObject
{
    private final FlyAbility bindingFlyAbility;

    public FlyChecker(FlyAbility flyAbility)
    {
        this.bindingFlyAbility = flyAbility;
    }

    private final Map<Player, PlayerFlyMeta> playerFlyMeta = new Object2ObjectArrayMap<>();

    @Initializer
    private void load(MorphConfigManager configManager)
    {
        this.addSchedule(this::acUpdate);

        configManager.bind(debugOutput, ConfigOption.DEBUG_OUTPUT);
    }

    private final Bindable<Boolean> debugOutput = new Bindable<>(false);

    private void acUpdate()
    {
        this.addSchedule(this::acUpdate);

        var toRemove = new ObjectArrayList<Player>();

        movementCheckTick++;

        playerFlyMeta.forEach((player, meta) ->
        {
            if (!player.isOnline())
                toRemove.add(player);

            // 减少VL
            var susVL = meta.suspectVL;
            meta.suspectVL = Math.max(0, susVL - 0.1d);
        });

        if (movementCheckTick >= movementCheckInterval.get())
            movementCheckTick = 0;

        if (toRemove.size() > 0)
        {
            toRemove.forEach(playerFlyMeta::remove);
            toRemove.clear();
        }

        //region Tick Player
    }

    private boolean doAdvancedCheck = false;
    private int movementCheckTick = 0;
    private final Bindable<Integer> movementCheckInterval = new Bindable<>(5);

    private static class PlayerFlyMeta
    {
        public PlayerFlyMeta(Player player)
        {
            bindingPlayer = player;
        }

        @NotNull
        private final Player bindingPlayer;

        public Player getBindingPlayer()
        {
            return bindingPlayer;
        }

        /**
         * 若此值不为0，则忽略一次检查并将此值减少1
         */
        public int ignoreNext;

        public boolean isSprinting = false;
        public boolean wasSprinting = false;

        public long lastTrigger = 0L;

        /**
         * Player suspect Level
         */
        public double suspectVL = 0d;

        /**
         * Player's last legal location before speeding
         */
        @Nullable
        public Location lastLegalLocation;

        /**
         * 玩家没有移动事件的时长
         */
        public int ticksNoMovementEvent = 0;

        // 最后一次移动的属性
        public boolean lastMoveHasHorizonal = false;
        public boolean lastMoveHasVertical = false;

        /**
         * 玩家是否在加速
         * @return
         */
        public boolean startingSprint()
        {
            return !wasSprinting && isSprinting;
        }

        /**
         * 玩家是否在减速
         * @return
         */
        public boolean stoppingSprint()
        {
            return wasSprinting && !isSprinting;
        }

        public final Recorder<Double> flyMult = new Recorder<>(0d);

        public boolean recorderNotFinal()
        {
            return flyMult.get() != 0d && flyMult.get() != 1d;
        }
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphManager manager;

    public void setLastLegalLocation(Player player, Location loc, boolean ignoreNextMovement)
    {
        var meta = playerFlyMeta.getOrDefault(player, new PlayerFlyMeta(player));
        meta.lastLegalLocation = loc;

        if (ignoreNextMovement)
            meta.ignoreNext += 5;
    }

    public void onEvent(PlayerMoveEvent e)
    {
        var player = e.getPlayer();
        var meta = playerFlyMeta.getOrDefault(player, null);

        // 提前设置和检查meta属性
        if (meta == null)
        {
            meta = new PlayerFlyMeta(player);
            playerFlyMeta.put(player, meta);
        }

        // 如果有ignoreNext，则忽略此次检查
        if (meta.ignoreNext > 0)
        {
            //tickDiff = MathUtils.max(2, meta.ignoreNext - 1, tickDiff);
            meta.ignoreNext--;
            return;
        }

        var distanceDelta = e.getFrom().distance(e.getTo());

        // 忽略静止不动的移动数据
        if (distanceDelta == 0)
            return;

        // Base multiplier
        var hasHorizonal = (e.getFrom().x() - e.getTo().x() != 0) || (e.getFrom().z() - e.getTo().z() != 0);
        var hasVertical = e.getFrom().y() - e.getTo().y() != 0;

        meta.lastMoveHasHorizonal = hasHorizonal;
        meta.lastMoveHasVertical = hasVertical;

        // 计算移动
        var maxMovement = 0d;

        //以下时经过测量的，各个状态下的实际速度vs速度属性的倍率
        //var horzMax = spd * 5.4444;
        //var vertMax = spd * 3.75;
        //var hav = spd * 6.6109;

        // 设置meta中的移动属性
        if (hasHorizonal) maxMovement += 5.4444;
        if (hasVertical) maxMovement += hasHorizonal ? 1.1704 : 3.75;

        var playerSprinting = player.isSprinting();

        // 设置meta中的疾行属性
        meta.wasSprinting = meta.isSprinting;
        meta.isSprinting = playerSprinting;

        // 根据疾行调整预期速度的倍率
        if (meta.isSprinting != meta.wasSprinting)
        {
            //logger.info("Do change transform");
            Transformer.transform(meta.flyMult, playerSprinting ? 1d : 0d, (playerSprinting ? 0 : 55) * 50L, Easing.Plain);
        }

        // 将乘数乘以飞行倍率
        maxMovement *= (1 + meta.flyMult.get() + (playerSprinting && hasHorizonal && hasVertical ? -0.25 : 0));

        if (player.isRiptiding())
        {
            //workaround: After riptide there is a process to slow down.
            //edit: Changed to 9.4 and <3 Mojang for not making f**king anti-cheat for their game.
            maxMovement *= 9.4;
        }

        // 如果玩家任意vector不等于0，那么隔5tick再检查
        //if (MathUtils.vectorNotZero(player.getVelocity()))
        //{
        //meta.ignoreNext = Math.max(meta.ignoreNext, 5);
        //return;
        //}

        // 获取配置的飞行速度
        var spd = bindingFlyAbility.getTargetFlySpeed(manager.getDisguiseStateFor(player).getDisguiseIdentifier());

        // 取得此玩家在1tick内最多可以飞行的距离
        // 计算方法为 【配置的飞行速度】x【此事件最大的移动距离】
        var threshold = spd * maxMovement;

        // 执行检查
        meta.lastTrigger = plugin.getCurrentTick();

        var diff = distanceDelta - threshold ; //Math.abs(travelled - spd * c);

        var tick = meta.suspectVL;

        if (tick <= 1d)
            meta.lastLegalLocation = e.getFrom();

        if (debugOutput.get())
        {
            var debugLine = "%s 本次 %.5f 阈值 %.5f 差异 %.5f 疾跑倍率 %s 当前Tick %s".formatted(player.getName(), distanceDelta, threshold, diff, meta.flyMult.get(), plugin.getCurrentTick());

            if (diff > 0.03)
                logger.warn(debugLine);
            else
                logger.info(debugLine);
        }

        if (diff > 0.03)
        {
            tick++;
            meta.suspectVL = tick;

            var playerName = player.getName();
            logger.info("检测到玩家 %s 的位移 %.5f 已超速 %.3f, 预期是 %.5f, 当前VL %.2f".formatted(playerName, distanceDelta, diff, threshold, tick));

            if (tick >= 5d)
            {
                logger.info("玩家 %s 的超速值为 %.3f VL, 正在拉回".formatted(playerName, tick));
                var loc = meta.lastLegalLocation == null ? e.getFrom() : meta.lastLegalLocation;
                bindingFlyAbility.ignoreNextTeleport = true;
                player.teleport(loc);
                //e.setCancelled(true);

                //if (tick >= 5d)
                //{
                //logger.info("Canceling disguise for player %s.".formatted(player.getName()));
                //manager.unMorph(MorphManager.nilCommandSource, player, true);

                //    meta.suspectVL = 0;
                //}
            }
        }

        //logger.info("Adding movement %.5f to %.5f".formatted(travelled, meta.distanceTravelled));
    }
}
