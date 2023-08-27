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
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.transforms.Recorder;
import xiamomc.morph.transforms.Transformer;
import xiamomc.morph.transforms.easings.Easing;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.List;
import java.util.Map;

public class FramedFlyChecker extends MorphPluginObject implements IFlyChecker
{
    private final xiamomc.morph.abilities.impl.FlyAbility bindingFlyAbility;

    public FramedFlyChecker(xiamomc.morph.abilities.impl.FlyAbility flyAbility)
    {
        this.bindingFlyAbility = flyAbility;
    }

    private final Map<Player, PlayerFlyMeta> playerFlyMeta = new Object2ObjectArrayMap<>();

    @Initializer
    private void load(MorphConfigManager configManager)
    {
        this.addSchedule(this::acUpdate);

        configManager.bind(debugOutput, ConfigOption.DEBUG_OUTPUT);
        debugOutput.set(true);
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

            //暂时禁用
            if (movementCheckTick >= movementCheckInterval.get())
            {
                // 实行新版检测机制
                double maxMovement = 0d;
                meta.distanceTravelled = 0d;

                for (PlayerFrame f : meta.frames)
                {
                    // 移动倍率
                    var moveMult = 0d;

                    // 设置meta中的移动属性
                    if (f.hasHorz) moveMult += 5.4406;
                    if (f.hasVert) moveMult += f.hasHorz ? 1.1704 : 3.75;

                    moveMult *= (1 + f.speedMult);

                    maxMovement += (f.flySpeed * moveMult) + 0.015;
                    meta.distanceTravelled += f.distance;

                    if (debugOutput.get())
                    {
                        var debugLine = "帧#%s: 本次 %.5f 已过 %.5f  阈值 %.5f 差异 %.5f 疾跑倍率 %s"
                                .formatted(meta.frames.indexOf(f), f.distance, meta.distanceTravelled,
                                        maxMovement,
                                        meta.distanceTravelled - maxMovement, f.speedMult);

                        if (meta.distanceTravelled - maxMovement > 0.015)
                            logger.warn(debugLine);
                        else
                            logger.info(debugLine);
                    }
                }

                meta.expectedMovementDistance = maxMovement;
                meta.frames.clear();

                // 忽略0移动
                if (meta.distanceTravelled == 0d) return;

                //check move
                if (debugOutput.get())
                    logger.info("玩家 %s 移动了 %.5f 格，限制是 %.5f 格".formatted(player.getName(), meta.distanceTravelled, meta.expectedMovementDistance));

                if (meta.distanceTravelled > meta.expectedMovementDistance)
                {
                    player.teleport(meta.lastLegalLocation == null ? player.getLocation() : meta.lastLegalLocation);

                    logger.warn("玩家 %s 移动了 %.5f 个方块超出了 %.5f 的限制, 正在拉回".formatted(player.getName(), meta.distanceTravelled, meta.expectedMovementDistance));
                }
            }
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

    private int movementCheckTick = 0;
    private final Bindable<Integer> movementCheckInterval = new Bindable<>(5);

    /**
     * 玩家的飞行帧
     */
    private static class PlayerFrame
    {
        public final boolean hasHorz;
        public final boolean hasVert;
        public final boolean isSprint;
        public final double distance;

        public final double speedMult;

        public final double flySpeed;

        public final long timestamp;

        public PlayerFrame(boolean hasHorz, boolean hasVert, boolean isSprint, double distance, double flySpeed, double speedMult, long timestamp)
        {
            this.hasHorz = hasHorz;
            this.hasVert = hasVert;
            this.isSprint = isSprint;
            this.distance = distance;

            this.speedMult = speedMult;
            this.flySpeed = flySpeed;

            this.timestamp = timestamp;
        }
    }

    private static class PlayerFlyMeta
    {
        public PlayerFlyMeta(Player player)
        {
            bindingPlayer = player;
        }

        @NotNull
        private final Player bindingPlayer;

        public final List<PlayerFrame> frames = new ObjectArrayList<>();

        public Player getBindingPlayer()
        {
            return bindingPlayer;
        }

        /**
         * 若此值不为0，则忽略一次检查并将此值减少1
         */
        public int ignoreNext;

        /**
         * 我们期待玩家移动的距离
         */
        public double expectedMovementDistance;

        public boolean isSprinting = false;
        public boolean wasSprinting = false;

        public boolean isUsingRiptide = false;
        public boolean wasUsingRiptide = false;

        /**
         * 玩家上次移动的时间
         */
        public long lastMove = 0L;

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

        public final Recorder<Double> flyMult = new Recorder<>(0d);
        public final Recorder<Double> riptideMult = new Recorder<>(0d);
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

    @Override
    public void dropMeta(Player player)
    {
        playerFlyMeta.remove(player);
    }

    private final boolean doNewCheck = true;

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

        meta.lastMove = plugin.getCurrentTick();

        var distanceDelta = e.getFrom().distance(e.getTo());
        var travelled = meta.distanceTravelled + distanceDelta;

        // 忽略静止不动的移动数据
        if (travelled == 0)
            return;

        // Base multiplier
        var hasHorizonal = (e.getFrom().x() - e.getTo().x() != 0) || (e.getFrom().z() - e.getTo().z() != 0);
        var hasVertical = e.getFrom().y() - e.getTo().y() != 0;

        var playerSprinting = player.isSprinting();

        // 设置meta中的疾行属性
        meta.wasSprinting = meta.isSprinting;
        meta.isSprinting = playerSprinting;

        // 根据疾行调整预期速度的倍率
        if (meta.isSprinting != meta.wasSprinting)
            Transformer.transform(meta.flyMult, playerSprinting ? 1d : 0d, (playerSprinting ? 5 : 55) * 50L, Easing.Plain);

        var riptiding = player.isRiptiding();
        meta.wasUsingRiptide = meta.isUsingRiptide;
        meta.isUsingRiptide = riptiding;

        if (meta.wasUsingRiptide != meta.isUsingRiptide)
            Transformer.transform(meta.riptideMult, riptiding ? 3d : 0d, (riptiding ? 0 : 55) * 50L, Easing.Plain);

        var spd = bindingFlyAbility.getTargetFlySpeed(manager.getDisguiseStateFor(player).getDisguiseIdentifier());

        // 写入Frame
        var frame = new PlayerFrame(hasHorizonal, hasVertical, player.isSprinting(), distanceDelta, spd, meta.flyMult.get() + meta.riptideMult.get(), System.currentTimeMillis());
        meta.frames.add(frame);
    }
}
