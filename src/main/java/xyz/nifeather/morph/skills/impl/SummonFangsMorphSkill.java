package xyz.nifeather.morph.skills.impl;

import org.bukkit.Difficulty;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.SkillStrings;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.NmsRecord;
import xyz.nifeather.morph.misc.mobs.MorphBukkitVexModifier;
import xyz.nifeather.morph.skills.MorphSkill;
import xyz.nifeather.morph.skills.SkillType;
import xyz.nifeather.morph.skills.options.NoOpConfiguration;
import xyz.nifeather.morph.storage.skill.SkillAbilityConfiguration;

public class SummonFangsMorphSkill extends MorphSkill<NoOpConfiguration>
{
    @Override
    public int executeSkill(Player player, DisguiseState state, SkillAbilityConfiguration configuration, NoOpConfiguration option)
    {
        var targetEntity = player.getTargetEntity(16);

        var summonVex = targetEntity != null
                && (player.isSneaking() || targetEntity.getLocation().distance(player.getLocation()) > 8);
        var world = player.getWorld();

        if (summonVex)
        {
            if (world.getDifficulty() == Difficulty.PEACEFUL)
            {
                sendDenyMessageToPlayer(player, SkillStrings.difficultyIsPeacefulString()
                        .withLocale(MessageUtils.getLocale(player))
                        .toComponent(null));

                return 10;
            }

            var isLiving = targetEntity instanceof LivingEntity;

            var location = player.getEyeLocation();
            var targetAmount = 3;

            this.scheduleOn(player, () ->
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
        else
        {
            var location = player.getLocation();
            var direction = player.getEyeLocation().getDirection();

            var targetFangs = 16;
            Location oldLocation = null;

            for (int i = 0; i < targetFangs; i++)
            {
                location.add(direction.getX(), 0, direction.getZ());

                //是否要寻找新方块
                if (world.getBlockAt(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ()).getType().isAir())
                {
                    var blockDown = world.rayTraceBlocks(location, new Vector(0, -1, 0), 8);
                    Block newBlock;

                    //根据玩家视角选择方向
                    newBlock = blockDown == null ? null : blockDown.getHitBlock();

                    //设置新位置
                    if (newBlock != null)
                    {
                        var newY = getTopY(newBlock);
                        location.setY(newY);
                    }
                    else
                        break;
                }

                if (oldLocation != null && oldLocation.getBlockY() == location.getBlockY())
                {
                    //trace方法有问题，尖刺不管多密集总是会在障碍物前面一格停止生成
                    var traceDirection = location.clone().subtract(oldLocation).toVector();

                    if (traceDirection.lengthSquared() > 0.0)
                    {
                        var traceResult = world.rayTraceBlocks(location, traceDirection, oldLocation.distance(location) + 1, FluidCollisionMode.NEVER, true);

                        if (traceResult != null)
                            break;
                    }
                }

                //设置位置
                var loc = location.clone();
                oldLocation = location.clone();

                //添加到计划任务
                this.scheduleOn(player, ()  ->
                {
                    var fang = world.spawn(loc, EvokerFangs.class, CreatureSpawnEvent.SpawnReason.CUSTOM);
                    fang.setOwner(player);
                }, i);
            }
        }

        return configuration.getCooldown();
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillType.EVOKER;
    }

    private final NoOpConfiguration option = new NoOpConfiguration();

    @Override
    public NoOpConfiguration getOptionInstance()
    {
        return option;
    }
}
