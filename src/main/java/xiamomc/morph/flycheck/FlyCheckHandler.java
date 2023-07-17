package xiamomc.morph.flycheck;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.MorphManager;
import xiamomc.morph.MorphPluginObject;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.transforms.Recorder;
import xiamomc.morph.transforms.Transformer;
import xiamomc.morph.transforms.easings.Easing;
import xiamomc.morph.utilities.MathUtils;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.Map;

public class FlyCheckHandler extends MorphPluginObject
{
    private final xiamomc.morph.abilities.impl.FlyAbility bindingFlyAbility;

    public FlyCheckHandler(xiamomc.morph.abilities.impl.FlyAbility flyAbility)
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

        timeLastUpdate = System.currentTimeMillis();

        playerFlyMeta.forEach((player, meta) ->
        {
            if (!player.isOnline())
                toRemove.add(player);

            var susVL = meta.suspectVL;
            var val = Math.max(0, susVL - 0.1d);
            meta.suspectVL = val;

            meta.wasSprinting = meta.isSprinting;
            meta.isSprinting = player.isSprinting();

            //暂时禁用
            if (doAdvancedCheck && movementCheckTick >= movementCheckInterval.get())
            {
                if (meta.distanceTravelled == 0d) return;

                //check move
                if (debugOutput.get())
                    logger.info("Player %s moved %.5f block in %.5f limit".formatted(player.getName(), meta.distanceTravelled, meta.expectedMovementDistance));

                if (meta.ignoreNext <= 0 && meta.distanceTravelled > meta.expectedMovementDistance)
                {
                    player.teleport(meta.lastLegalLocation == null ? player.getLocation() : meta.lastLegalLocation);

                    logger.warn("Player %s travelling %.5f blocks speeding over %.5f limit, dragging back".formatted(player.getName(), meta.distanceTravelled, meta.expectedMovementDistance));
                }

                meta.expectedMovementDistance = 0d;
                meta.distanceTravelled = 0d;
            }

            if (!doAdvancedCheck)
                meta.distanceTravelled = 0d;

            meta.eventDuplicates = 0;
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

    private long timeLastUpdate = 0L;

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

        public long tickLastSprint = 0L;

        public long tickStartedSprint = 0L;

        public boolean isSprinting = false;
        public boolean wasSprinting = false;
        public int ignoreNext = 0;

        public int eventDuplicates = 0;
        public long lastTrigger = 0L;

        /**
         * 玩家上次移动的时间
         */
        public long lastMove = 0L;

        /**
         * 我们期望此玩家在给定时间内移动的距离
         */
        public double expectedMovementDistance = 0d;

        /**
         * 此玩家实际移动的距离
         */
        public double distanceTravelled = 0d;

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

    public void doCheck(PlayerMoveEvent e)
    {
        var player = e.getPlayer();
        var meta = playerFlyMeta.getOrDefault(player, null);

        // 提前设置和检查meta属性
        if (meta == null)
        {
            meta = new PlayerFlyMeta(player);
            playerFlyMeta.put(player, meta);
        }

        if (meta.ignoreNext > 0)
        {
            //tickDiff = MathUtils.max(2, meta.ignoreNext - 1, tickDiff);
            meta.ignoreNext--;
            return;
        }

        meta.lastMove = plugin.getCurrentTick();

        var distanceDelta = e.getFrom().distance(e.getTo());
        //var travelled = meta.distanceTravelled + distanceDelta; //因为mc的奇特原因注释掉了
        var travelled = meta.distanceTravelled + distanceDelta;

        // 忽略静止不动的移动数据
        if (travelled == 0)
            return;

        // Base multiplier
        var hasHorizonal = (e.getFrom().x() - e.getTo().x() != 0) || (e.getFrom().z() - e.getTo().z() != 0);
        var hasVertical = e.getFrom().y() - e.getTo().y() != 0;

        meta.lastMoveHasHorizonal = hasHorizonal;
        meta.lastMoveHasVertical = hasVertical;

        // 计算移动
        var c = 0d;

        // 设置meta中的移动属性
        if (meta.lastMoveHasHorizonal) c += 5.4406;
        if (meta.lastMoveHasVertical) c += meta.lastMoveHasHorizonal ? 1.1704 : 3.75;

        var playerSprinting = player.isSprinting();

        // 设置meta中的疾行属性
        meta.isSprinting = playerSprinting;
        if (playerSprinting)
        {
            if (!meta.isSprinting) meta.tickStartedSprint = plugin.getCurrentTick();
            meta.tickLastSprint = plugin.getCurrentTick();
        }

        // 玩家取消疾行：缓慢降低飞行速度倍率到正常水平
        if (meta.isSprinting != meta.wasSprinting)
        {
            //logger.info("Do change transform");
            Transformer.transform(meta.flyMult, playerSprinting ? 1d : 0d, (playerSprinting ? 0 : 55) * 50L, Easing.Plain);
        }

        // 将乘数乘以飞行倍率
        c *= Math.max(1, 2 * meta.flyMult.get());

        if (player.isRiptiding())
        {
            //workaround: After riptide there is a process to slow down.
            //edit: Changed to 9.4 and <3 Mojang for not making f**king anti-cheat for their game.
            c *= 9.4;
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
        var threshold = spd * c * 1;

        // 增加meta中我们期望的速度
        meta.expectedMovementDistance += threshold;

        //check

        //以下时经过测量的，各个状态下的实际速度vs速度属性的倍率
        //var horzMax = spd * 5.4406;
        //var vertMax = spd * 3.75;
        //var hav = spd * 6.611;

        // timeDiff: 事件到来时和上次更新的时差
        // tickDiff: 事件到来时和上次更新的Tick差距（ 四舍五入 ）
        var timeDiff = System.currentTimeMillis() - timeLastUpdate;
        var tickDiff = Math.max(1, Math.round((float) timeDiff / 50L));

        // 如果一个tick内触发了多次，那么增加tickDiff
        var currrentTick = plugin.getCurrentTick();
        if (meta.lastTrigger == currrentTick)
        {
            meta.eventDuplicates++;
            tickDiff += meta.eventDuplicates;
            //meta.ignoreNext++;
        }

        meta.lastTrigger = plugin.getCurrentTick();

        var diff = travelled - threshold ; //Math.abs(travelled - spd * c);

        var tick = meta.suspectVL;

        if (tick <= 0d)
        {
            //logger.info("Updating last legal position: " + tick);
            meta.lastLegalLocation = e.getFrom();
        }

        if (debugOutput.get())
        {
            var debugLine = "A %.5f T %.5f D %.5f, F %s GT %s Sys %s TIMEDIFF %s TICKDIFF %s Tick".formatted(travelled, threshold, diff, meta.flyMult.get(), plugin.getCurrentTick(), System.currentTimeMillis(),timeDiff, tickDiff);
            if (diff > 0.05)
                logger.warn(debugLine);
            else
                logger.info(debugLine);
        }

        if (diff > (meta.recorderNotFinal() ? 0.05 : 0.05))
        {
            tick++;
            meta.suspectVL = tick;

            var playerName = player.getName();
            logger.info("Speeding detected for player %s violating over %s".formatted(playerName, diff));

            if (tick >= 5d)
            {
                logger.info("Player %s is speeding for %.3f VL, dragging back".formatted(playerName, tick));
                var loc = meta.lastLegalLocation == null ? e.getFrom() : meta.lastLegalLocation;
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

        if (doAdvancedCheck)
            meta.expectedMovementDistance += threshold;

        meta.distanceTravelled += travelled;
    }
}
