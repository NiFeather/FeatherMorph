package xiamomc.morph;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.network.commands.S2C.set.S2CSetRevealingCommand;
import xiamomc.morph.network.server.MorphClientHandler;
import xiamomc.morph.utilities.MathUtils;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Annotations.Resolved;
import xiamomc.pluginbase.Bindables.Bindable;

import java.util.Map;

public class RevealingHandler extends MorphPluginObject
{
    /**
     * 获取此State的揭示等级
     */
    public RevealingLevel getRevealingLevel(Player player)
    {
        return this.getRevealingState(player).getRevealingLevel();
    }

    /**
     * 获取此State的揭示值
     */
    public float getRevealingValue(Player player)
    {
        return this.getRevealingState(player).getBaseValue();
    }

    public RevealingState getRevealingState(Player player)
    {
        var state = playerRevealingStateMap.getOrDefault(player, null);

        if (state == null)
        {
            state = new RevealingState(player);
            playerRevealingStateMap.put(player, state);
        }

        return state;
    }

    private final Map<Player, RevealingState> playerRevealingStateMap = new Object2ObjectArrayMap<>();

    @Initializer
    private void load()
    {
        this.addSchedule(this::update);
    }

    private void update()
    {
        this.addSchedule(this::update);

        var decay = plugin.getCurrentTick() % 5 == 0;
        for (var state : this.playerRevealingStateMap.values())
        {
            //每两秒衰减1点
            if (decay)
                state.addBaseValue(RevealingDiffs.NATURAL_DECAY, true);

            //否则，如果等级小于怀疑等级的50%，增加0.01
            //if (state.getBaseValue() < RevealingLevel.SUSPECT.val * 0.2f)
            //    state.addBaseValue(RevealingDiffs.NATURAL_INCREASEMENT);
        }
    }

    /**
     * 某个玩家的揭示状态
     */
    public static class RevealingState extends MorphPluginObject
    {
        //和此State对应的玩家
        private final Player player;

        public DisguiseState bindingState;

        public RevealingState(Player player)
        {
            this.player = player;
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

            this.baseValue.set(newVal);
            this.revealingLevel = null;

            clientHandler.sendCommand(player, new S2CSetRevealingCommand(newVal));
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
    }

    /**
     * 揭示等级
     */
    public enum RevealingLevel
    {
        NORMAL(0),
        SUSPECT(30),
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

            return keyArray.size() == 0 ? NORMAL : valueLevelMap
                    .getOrDefault(keyArray.get(keyArray.size() - 1), NORMAL);
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
        public static final float NATURAL_DECAY = -(NATURAL_DIFFBASE * 0.5f) / 5;

        /**
         * 自然递增
         */
        public static final float NATURAL_INCREASEMENT = (NATURAL_DIFFBASE / 40) * 2;

        /**
         * 被生物注意
         */
        public static final float ON_MOB_TARGET = 0.36f;

        /**
         * 受伤
         */
        public static final float ON_DAMAGE = 1.5f;

        /**
         * 和任意方块互动
         */
        public static final float INTERACT = 1f;

        /**
         * 和任意实体互动
         */
        public static final float INTERACT_ENTITY = 1f;

        /**
         * 破坏金块（猪灵）
         */
        public static final int BREAK_GOLD_BLOCK = 20;

        /**
         * 主动破坏方块
         */
        public static final float BLOCK_BREAK = 1.4f;

        /**
         * 主动放置方块
         */
        public static final float BLOCK_PLACE = 1.4f;

        /**
         * 变形时已经被生物当作目标
         */
        public static final int ALREADY_TARGETED = Math.round(RevealingLevel.REVEALED.val / 2);
    }
}
