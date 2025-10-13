package xyz.nifeather.morph.skills.impl;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.api.morphs.skills.SkillNames;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.SkillStrings;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.ExecutionErrorException;
import xyz.nifeather.morph.misc.NmsRecord;
import xyz.nifeather.morph.misc.mobs.MorphBukkitVexHolder;
import xyz.nifeather.morph.skills.options.NoOpConfiguration;

import java.util.List;

public class EvokerMorphSkill extends DelayedMorphSkill<NoOpConfiguration>
{
    @Override
    public ISkillAbilityOptionHandler<NoOpConfiguration> optionHandler()
    {
        return NoOpConfiguration.OPTION_HANDLER;
    }

    public static final String SESSION_DATA_SUMMON_VEX = "EVOKER_SKILL_SUMMON_VEX";
    public static final String SESSION_DATA_VEX_LIST = "EVOKER_SKILL_VEX_LIST";

    public record EvokerSkillDataRecord(boolean shouldSummonVex, @Nullable Entity lastTargetedEntity)
    {
    }

    public static class VexHolderCounter
    {
        private final List<MorphBukkitVexHolder> list = ObjectLists.synchronize(new ObjectArrayList<>());

        public VexHolderCounter()
        {
        }

        public void addHolder(MorphBukkitVexHolder vex)
        {
            list.add(vex);

        }

        public void removeHolder(MorphBukkitVexHolder vex)
        {
            list.remove(vex);
        }

        public int countAlive()
        {
            return list.stream().filter(holder -> !holder.getVex().isDead()).toArray().length;
        }

        /**
         * <b>Please remember to see apiNote*</b>
         * @apiNote The amount of alive may not be updated after this call
         *          If you wish to call an immediate kill, see {@link VexHolderCounter#kill(int, boolean)}
         */
        public void kill(int amount)
        {
            kill(amount, false);
        }

        /**
         * Kill X Vex(es) being tracked by this counter
         * @param amount The amount
         * @param immediate Whether to immediate kill the vex, <b>REMEMBER</b> to ensure we are on the correct thread (The entity's thread) or it will throw error on Folia
         */
        public void kill(int amount, boolean immediate)
        {
            if (list.size() < amount)
                amount = list.size();

            //FeatherMorphMain.getInstance().getSLF4JLogger().info("TO KILL " + amount);

            for (int i = 0; i < amount; i++)
            {
                var holder = list.removeFirst();
                var vex = holder.getVex();

                if (immediate)
                    vex.setHealth(0);
                else
                    vex.getScheduler().run(FeatherMorphMain.getInstance(), task -> vex.setHealth(0), () -> {});

                // Ensure this entity got removed
                vex.getScheduler().runDelayed(FeatherMorphMain.getInstance(), task ->
                {
                    if (!vex.isDead()) vex.remove();
                }, () -> {}, 60);
            }

            trim();
        }

        public void killAll()
        {
            kill(list.size());
        }

        public void trim()
        {
            list.removeIf(holder -> holder.getVex().isDead());
        }
    }

    @Override
    public void onDeEquip(DisguiseState state)
    {
        super.onDeEquip(state);

        //logger.info("DeEquip! " + state.getPlayer().getName());

        var counter = state.getSessionData(SESSION_DATA_VEX_LIST, VexHolderCounter.class);
        if (counter != null)
        {
            //logger.info("Counter Not Null! Kill all...");
            counter.killAll();
        }

        state.removeSessionData(SESSION_DATA_VEX_LIST);
    }

    @Override
    public void onInitialEquip(DisguiseState state)
    {
        super.onInitialEquip(state);
        state.setSessionData(SESSION_DATA_VEX_LIST, new VexHolderCounter());
    }

    private VexHolderCounter getVexCounter(DisguiseState state)
    {
        var counter = state.getSessionData(SESSION_DATA_VEX_LIST, VexHolderCounter.class);
        if (counter == null)
            throw new NullDependencyException("VexHolderCounter for %s's DisguiseState is NULL!".formatted(state.getPlayer().getName()));

        return counter;
    }

    @Override
    protected ExecuteResult preExecute(Player player, DisguiseState state, @NotNull NoOpConfiguration option) throws ExecutionErrorException
    {
        var summonVex = player.isSneaking();

        if (summonVex && player.getWorld().getDifficulty() == Difficulty.PEACEFUL)
        {
            sendDenyMessageToPlayer(player, SkillStrings.difficultyIsPeacefulString());

            return ExecuteResult.fail(10);
        }

        Key soundKey = summonVex
                ? Key.key("entity.evoker.prepare_summon")
                : Key.key("entity.evoker.prepare_attack");

        playSoundToNearbyPlayers(player, 16,
                soundKey, Sound.Source.HOSTILE);

        state.setSessionData(SESSION_DATA_SUMMON_VEX, new EvokerSkillDataRecord(summonVex, player.getTargetEntity(16)));
        state.getDisguiseWrapper().setAggressive(true);
        return ExecuteResult.success(0);
    }

    @Override
    protected int getExecuteDelay(NoOpConfiguration option)
    {
        return 20;
    }

    private void doSummonVex(Player player, Entity targetEntity, DisguiseState state)
    {
        var world = player.getWorld();

        if (world.getDifficulty() == Difficulty.PEACEFUL)
            return;

        var isLiving = targetEntity instanceof LivingEntity;

        var location = player.getEyeLocation();

        int maximumVexAmount = 6;

        var vexCounter = this.getVexCounter(state);
        var targetAmount = 3;

        if (vexCounter.countAlive() + targetAmount > maximumVexAmount)
            vexCounter.kill(3);

        this.scheduleAt(player.getLocation(), () ->
        {
            for (int i = 0; i < targetAmount; i++)
            {
                var vex = world.spawn(location, Vex.class, CreatureSpawnEvent.SpawnReason.CUSTOM);
                vexCounter.addHolder(new MorphBukkitVexHolder(vex, player));

                vex.setLimitedLifetimeTicks(20 * (30 + NmsRecord.ofPlayer(player).random.nextInt(90)));

                if (isLiving)
                    vex.setTarget((LivingEntity) targetEntity);

                vex.setPersistent(false);
            }
        });
    }

    /**
     * 从给定的位置开始，寻找第一个上表面没有碰撞的方块
     * @param startingLocation 起始位置
     * @param step 步进，大于0为向上，小于0为向下
     * @param maxY 最大Y
     * @param minY 最小Y
     * @return 寻找到的方块的位置，如果没找到则是NULL
     */
    @Nullable
    private Location traceForFangLocation(Location startingLocation, int step, double maxY, double minY)
    {
        var world = startingLocation.getWorld();
        var currentLocation = startingLocation.clone();

        Location foundLocation = null;
        while (currentLocation.getY() >= minY && currentLocation.getY() <= maxY)
        {
            // 获取当前位置下方的方块：如果下方方块存在碰撞，则我们可以在他的上表面生成
            var blockBelow = world.getBlockAt(currentLocation.clone().add(0, -1, 0));

            // 获取当前方块
            var blockCurrent = world.getBlockAt(currentLocation);

            // 如果当前方块没有碰撞但是下面有，则允许生成
            if (!blockCurrent.isCollidable() && blockBelow.isCollidable())
            {
                // 获取下方方块最高碰撞箱的Y
                var boundingBoxMaxY = 0d;
                for (BoundingBox boundingBox : blockBelow.getCollisionShape().getBoundingBoxes())
                {
                    if (boundingBox.getMaxY() > boundingBoxMaxY)
                        boundingBoxMaxY = boundingBox.getMaxY();
                }

                // 设定Y值
                var found = currentLocation.clone();
                found.setY(blockBelow.getY() + boundingBoxMaxY);

                foundLocation = found;
                //logger.info("Current is %s And Below Is %s, Found At (%s, %s, %s)".formatted(blockCurrent.getType(), blockBelow.getType(), foundLocation.getX(), foundLocation.getY(), foundLocation.getZ()));
                break;
            }

            currentLocation.add(0, step, 0);
        }

        return foundLocation;
    }

    private void doSummonFangs(Player player, @Nullable Entity targetEntity)
    {
        var playerLocation = player.getLocation();
        var eyeDirection = scaleVector2D(player.getEyeLocation().getDirection());
        var world = player.getWorld();

        var targetFangs = 16;

        var maxY = player.getY();
        var minY = player.getY();

        if (targetEntity != null)
        {
            // 存在目标实体时，我们要求路径必须连贯完整
            maxY = Math.max(maxY, targetEntity.getY()) + 1;
            minY = Math.min(minY, targetEntity.getY()) - 1;
        }
        else
        {
            // 没有目标实体时，让限制更宽松一些
            maxY += 1;
            minY -= 5;
        }

        // 目标实体是否比我们更高？
        // 如果是，则我们需要从上往下追踪方块
        // 否则，从下往上找方块
        // todo: 但是！！！原版中的唤魔者只会从上往下找方块生成尖牙，我们真的需要从下往上增加复杂度吗？
        var targetLocationIsHigher = targetEntity == null || targetEntity.getLocation().getY() > playerLocation.getY();

        int stepDirection = targetLocationIsHigher ? -1 : 1;

        for (int fangIndex = 1; fangIndex <= targetFangs; fangIndex++)
        {
            var traceStartLocation = playerLocation.clone().add(new Vector(eyeDirection.getX() * fangIndex, 0, eyeDirection.getZ() * fangIndex));

            traceStartLocation.setY(targetLocationIsHigher ? maxY : minY);

            var targetLocation = this.traceForFangLocation(traceStartLocation, stepDirection, maxY, minY);

            if (targetLocation == null)
                break;

            // 生成实体
            var fang = world.spawn(targetLocation, EvokerFangs.class, CreatureSpawnEvent.SpawnReason.CUSTOM);
            fang.setAttackDelay(fangIndex);
            fang.setOwner(player);
        }
    }

    @Override
    public void executeDelayedSkill(Player player, DisguiseState state, NoOpConfiguration option)
    {
        state.getDisguiseWrapper().setAggressive(false);

        var skillData = state.getSessionDataOr(SESSION_DATA_SUMMON_VEX, EvokerSkillDataRecord.class, new EvokerSkillDataRecord(false, null));
        state.removeSessionData(SESSION_DATA_SUMMON_VEX);

        if (skillData.shouldSummonVex)
            this.doSummonVex(player, skillData.lastTargetedEntity, state);
        else
            this.doSummonFangs(player, player.getTargetEntity(16));
    }

    /**
     * 将给定的向量在二维空间上缩放到至少有一个值是 1 或 -1
     */
    private Vector scaleVector2D(Vector vec)
    {
        var maxAbs = Math.max(Math.abs(vec.getX()), Math.abs(vec.getZ()));

        return vec.clone().divide(new Vector(maxAbs, 1, maxAbs));
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillNames.EVOKER;
    }
}
