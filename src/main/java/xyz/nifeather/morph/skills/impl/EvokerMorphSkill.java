package xyz.nifeather.morph.skills.impl;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.SkillStrings;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.NmsRecord;
import xyz.nifeather.morph.misc.mobs.MorphBukkitVexModifier;
import xyz.nifeather.morph.api.morphs.skills.SkillNames;
import xyz.nifeather.morph.skills.options.NoOpConfiguration;
import xyz.nifeather.morph.storage.skill.SkillAbilityConfiguration;

public class EvokerMorphSkill extends DelayedMorphSkill<NoOpConfiguration>
{
    public static final String SESSION_DATA_SUMMON_VEX = "EVOKER_SKILL_SUMMON_VEX";

    private record EvokerSkillDataRecord(boolean shouldSummonVex, @Nullable Entity lastTargetedEntity)
    {
    }

    @Override
    protected ExecuteResult preExecute(Player player, DisguiseState state, SkillAbilityConfiguration configuration, NoOpConfiguration option)
    {
        var summonVex = player.isSneaking();

        if (summonVex && player.getWorld().getDifficulty() == Difficulty.PEACEFUL)
        {
            sendDenyMessageToPlayer(player, SkillStrings.difficultyIsPeacefulString()
                    .withLocale(MessageUtils.getLocale(player))
                    .toComponent(null));

            return ExecuteResult.fail(10);
        }

        Key soundKey = summonVex
                ? Key.key("entity.evoker.prepare_summon")
                : Key.key("entity.evoker.prepare_attack");

        playSoundToNearbyPlayers(player, 16,
                soundKey, Sound.Source.HOSTILE);

        state.setSessionData(SESSION_DATA_SUMMON_VEX, new EvokerSkillDataRecord(summonVex, player.getTargetEntity(16)));
        state.getDisguiseWrapper().setAggressive(true);
        return ExecuteResult.success(configuration.getCooldown());
    }

    @Override
    protected int getExecuteDelay(SkillAbilityConfiguration configuration, NoOpConfiguration option)
    {
        return 20;
    }

    private void doSummonVex(Player player, Entity targetEntity)
    {
        var world = player.getWorld();

        if (world.getDifficulty() == Difficulty.PEACEFUL)
            return;

        var isLiving = targetEntity instanceof LivingEntity;

        var location = player.getEyeLocation();
        var targetAmount = 3;

        this.scheduleAt(player.getLocation(), () ->
        {
            for (int i = 0; i < targetAmount; i++)
            {
                var vex = world.spawn(location, Vex.class, CreatureSpawnEvent.SpawnReason.CUSTOM);
                new MorphBukkitVexModifier(vex, player);

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
    public void executeDelayedSkill(Player player, DisguiseState state, SkillAbilityConfiguration configuration, NoOpConfiguration option)
    {
        state.getDisguiseWrapper().setAggressive(false);

        var skillData = state.getSessionDataOr(SESSION_DATA_SUMMON_VEX, EvokerSkillDataRecord.class, new EvokerSkillDataRecord(false, null));
        state.removeSessionData(SESSION_DATA_SUMMON_VEX);

        if (skillData.shouldSummonVex)
            this.doSummonVex(player, skillData.lastTargetedEntity);
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

    private final NoOpConfiguration option = new NoOpConfiguration();

    @Override
    public NoOpConfiguration getOptionInstance()
    {
        return option;
    }
}
