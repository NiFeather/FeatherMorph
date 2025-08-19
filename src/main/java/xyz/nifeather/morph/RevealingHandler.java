package xyz.nifeather.morph;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.network.commands.S2C.set.S2CSetMobRevealCommand;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.utilities.MathUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class RevealingHandler extends MorphPluginObject
{
    /**
     * 获取此State的揭示等级
     */
    @NotNull
    public RevealingLevel getRevealingLevel(Player player)
    {
        return this.getRevealingState(player).getRevealingLevel();
    }

    /**
     * 该玩家是否暴露？
     */
    public boolean shouldMobsAwareRevealed(Player player)
    {
        return this.getRevealingState(player).shouldMobsAwareRevealed();
    }

    /**
     * 获取此State的揭示值
     */
    public float getRevealingValue(Player player)
    {
        return this.getRevealingState(player).getBaseValue();
    }

    public void updateStatePlayerInstance(Player newInstance)
    {
        var match = playerRevealingStateMap.keySet().stream()
                .filter(k -> k.equals(newInstance.getUniqueId()))
                .findFirst().orElse(null);

        if (match == null) return;

        var state = playerRevealingStateMap.get(match);
        state.player = newInstance;

        playerRevealingStateMap.remove(match);
        playerRevealingStateMap.put(newInstance.getUniqueId(), state);
    }

    private static final Random seedRandom = new Random();

    @NotNull
    public RevealingState getRevealingState(Player player)
    {
        var state = playerRevealingStateMap.getOrDefault(player.getUniqueId(), null);

        if (state == null)
        {
            state = new RevealingState(player, seedRandom.nextInt());
            playerRevealingStateMap.put(player.getUniqueId(), state);
        }

        return state;
    }

    private final Map<UUID, RevealingState> playerRevealingStateMap = new ConcurrentHashMap<>();

    @Initializer
    private void load()
    {
        this.addSchedule(this::update);
    }

    private void update()
    {
        this.addSchedule(this::update);

        if (this.playerRevealingStateMap.isEmpty())
            return;

        // Remove offline players
        if (plugin.getCurrentTick() % 5 == 0)
        {
            var playersToRemove = new ArrayList<UUID>();

            playerRevealingStateMap.forEach((uuid, state) ->
            {
                var player = Bukkit.getPlayer(uuid);

                if (player == null)
                {
                    if (state.bindingState == null)
                    {
                        playersToRemove.add(uuid);
                        return;
                    }

                    if (state.bindingState.disposed())
                        playersToRemove.add(uuid);
                }
            });

            playersToRemove.forEach(playerRevealingStateMap::remove);
        }

        var decay = plugin.getCurrentTick() % 5 == 0;
        for (var state : this.playerRevealingStateMap.values())
        {
            //每两秒衰减1点
            if (decay)
                state.addBaseValue((state.bindingState == null ? 2 : 1) * RevealingDiffs.NATURAL_INCREASEMENT, true);

            //否则，如果等级小于怀疑等级的50%，增加0.01
            //if (state.getBaseValue() < RevealingLevel.SUSPECT.val * 0.2f)
            //    state.addBaseValue(RevealingDiffs.NATURAL_INCREASEMENT);

            if (plugin.getCurrentTick() % 10 == 0)
                state.notifyUpdates();
        }
    }

    /**
     * 某个玩家的揭示状态
     */
    public static class RevealingState extends MorphPluginObject
    {
        // 和此State对应的玩家
        private Player player;

        private final int randomSeed;

        // 根据当前揭示值确定玩家对于生物是否处于暴露阶段
        // 如果伪装揭示值已满，则始终允许生物target玩家
        // 否则，生物将有一定概率target玩家
        public boolean shouldMobsAwareRevealed()
        {
            var revealingLevel = this.getRevealingLevel();

            if (revealingLevel == RevealingLevel.SAFE)
                return false;

            if (revealingLevel == RevealingLevel.REVEALED)
                return true;

            // 通过 randomSeed + revAsLong 作为种子，这样我们可以确保在同一个揭示值(baseValue)的情况下，每次调用 shouldRevealToMobs 的输出都会一致
            long revAsLong = Math.round(this.baseValue.get() * 1000d);
            var random = new Random(randomSeed + revAsLong);
            float triggerValue = this.getBaseValue();

            // 随机值 + 20，避免刚到 SUSPECT 等级就被生物gank
            float randomNext = random.nextFloat(0f, 100f) + 20f;
            //player.sendActionBar(Component.text("nextFloat: %s | limit: %s | revAsLong: %s".formatted(randomNext, triggerValue, revAsLong)));

            return randomNext <= triggerValue;
        }

        @Nullable
        public DisguiseState bindingState;

        private final AtomicBoolean dirty = new AtomicBoolean(false);

        public RevealingState(Player player, int randomSeed)
        {
            this.player = player;
            this.randomSeed = randomSeed;
        }

        /**
         * 揭示基值
         */
        public final Bindable<Float> baseValue = new Bindable<>(0f);

        @Nullable
        private RevealingLevel revealingLevel;

        @Resolved(shouldSolveImmediately = true)
        private MorphClientHandler clientHandler;

        /**
         * 获取此State的揭示等级
         */
        @NotNull
        public RevealingLevel getRevealingLevel()
        {
            if (revealingLevel == null) revealingLevel = RevealingLevel.fromValue(baseValue);
            return revealingLevel;
        }

        /**
         * 获取此State的揭示基值
         */
        public float getBaseValue()
        {
            return baseValue.get();
        }

        /**
         * 设置此State的揭示基值
         */
        public void setBaseValue(float newVal)
        {
            newVal = MathUtils.clamp(0, 100, newVal);

            if (!dirty.get())
                dirty.set(newVal != baseValue.get());

            this.baseValue.set(newVal);
            this.revealingLevel = null;
        }

        public void addBaseValue(float diff)
        {
            addBaseValue(diff, false);
        }

        /**
         * @param forceAllowPositiveDiff 是否允许强制增加揭示基值
         */
        public void addBaseValue(float diff, boolean forceAllowPositiveDiff)
        {
            if (bindingState == null && diff > 0 && !forceAllowPositiveDiff) return;

            this.setBaseValue(MathUtils.clamp(0, 100, baseValue.get() + diff));
        }

        /**
         * 设置此State的揭示等级
         */
        public void setRevealingLevel(RevealingLevel newLv)
        {
            this.setBaseValue(newLv.getValue());
            this.revealingLevel = newLv;
        }

        public void notifyUpdates()
        {
            if (!dirty.get())
                return;

            clientHandler.sendCommand(player, new S2CSetMobRevealCommand(baseValue.get()));
            dirty.set(false);
        }
    }

    /**
     * 揭示等级
     */
    public enum RevealingLevel
    {
        SAFE(0),
        SUSPECT(20),
        REVEALED(80);

        private final float val;

        RevealingLevel(float val)
        {
            this.val = val;
        }

        /**
         * 获取达到此Level的最小揭示值
         */
        public float getValue()
        {
            return val;
        }

        private static final Map<Float, RevealingLevel> valueLevelMap = new Object2ObjectArrayMap<>();

        static
        {
            for (RevealingLevel value : RevealingLevel.values())
                valueLevelMap.put(value.val, value);
        }

        public static RevealingLevel fromValue(Bindable<? extends Number> bindable)
        {
            return fromValue(bindable.get().floatValue());
        }

        public static RevealingLevel fromValue(float val)
        {
            // 找到第一个比val小的值
            var keyArray = valueLevelMap.keySet().stream()
                    .filter(f -> val > f).toList();

            return keyArray.isEmpty()
                    ? SAFE
                    : valueLevelMap.getOrDefault(keyArray.getLast(), SAFE);
        }
    }

    /**
     * 各事件类型的揭示值变化
     */
    public static class RevealingDiffs
    {
        public static final float NATURAL_DIFFBASE = 1;

        /**
         * 自然衰减
         */
        public static final float NATURAL_DECAY = 0.25f;

        /**
         * 自然递增
         */
        public static final float NATURAL_INCREASEMENT = -NATURAL_DIFFBASE / 10;

        /**
         * 被生物注意
         */
        public static final float ON_MOB_TARGET = 0.36f;

        /**
         * 受伤
         */
        public static final float TAKE_DAMAGE = 1.5f;

        public static final float DEAL_DAMAGE = TAKE_DAMAGE / 4;

        /**
         * 和任意方块互动
         */
        public static final float INTERACT = 0.4f;

        /**
         * 和任意实体互动
         */
        public static final float INTERACT_ENTITY = 0.4f;

        /**
         * 主动破坏方块
         */
        public static final float BLOCK_BREAK = 0.45f;

        /**
         * 主动放置方块
         */
        public static final float BLOCK_PLACE = 0.45f;

        /**
         * 变形时已经被生物当作目标
         */
        public static final int ALREADY_TARGETED = Math.round(RevealingLevel.REVEALED.val / 2);
    }
}
