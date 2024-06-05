package xiamomc.morph.misc.mobs;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.event.entity.EntityTargetEvent;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Objects;

public class MorphVex extends Vex
{
    public MorphVex(@NotNull Player owner, EntityType<? extends Vex> type, Level world)
    {
        super(type, world);

        Objects.requireNonNull(owner, "Summoning MorphVex with Null owner?!");

        this.owner = owner;
    }

    @Override
    public void setOwner(Mob owner)
    {
        throw new NotImplementedException("setOwner() is not implemented with our custom impl.");
    }

    @Override
    public Mob getOwner()
    {
        throw new NotImplementedException("getOwner() is not implemented with our custom impl.");
    }

    public LivingEntity getPlayerOwner()
    {
        return owner;
    }

    @NotNull
    private final LivingEntity owner;

    @Override
    protected void registerGoals()
    {
        super.registerGoals();

        // 移除原版带有的Goal
        var targetSelectors = this.targetSelector.getAvailableGoals();
        for (var wrapped : targetSelectors) if (wrapped != null)
            this.targetSelector.removeGoal(wrapped.getGoal());

        // 并添加我们自己的Goal
        this.targetSelector.addGoal(0, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
    }

    public void setTarget(org.bukkit.entity.LivingEntity bukkitLiving)
    {
        var nmsLiving = ((CraftLivingEntity)bukkitLiving).getHandle();

        this.setTarget(nmsLiving, EntityTargetEvent.TargetReason.CUSTOM, true);
    }

    private static class OwnerHurtByTargetGoal extends TargetGoal
    {
        @NotNull
        private LivingEntity owner()
        {
            return thisEntity.getPlayerOwner();
        }

        private final MorphVex thisEntity;

        public OwnerHurtByTargetGoal(MorphVex thisEntity)
        {
            super(thisEntity, false);

            this.thisEntity = thisEntity;
        }

        @Override
        public boolean canUse()
        {
            var owner = this.owner();

            var lastHurtBy = owner.getLastHurtByMob();
            this.ownerLastHurtBy = lastHurtBy;

            return lastHurtBy != null
                    && this.canAttack(lastHurtBy, TargetingConditions.DEFAULT)
                    && ownerLastHurtByMobTimestamp != owner.getLastHurtByMobTimestamp();
        }

        private int ownerLastHurtByMobTimestamp;
        private LivingEntity ownerLastHurtBy;

        @Override
        public void start()
        {
            super.start();

            var owner = this.owner();
            this.ownerLastHurtByMobTimestamp = owner.getLastHurtByMobTimestamp();

            this.thisEntity.setTarget(ownerLastHurtBy, EntityTargetEvent.TargetReason.CUSTOM, true);
        }
    }

    private static class OwnerHurtTargetGoal extends TargetGoal
    {
        @NotNull
        private LivingEntity owner()
        {
            return thisEntity.getPlayerOwner();
        }

        private final MorphVex thisEntity;

        public OwnerHurtTargetGoal(MorphVex thisEntity)
        {
            super(thisEntity, false);

            Objects.requireNonNull(thisEntity, "Null Entity");

            this.thisEntity = thisEntity;

            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse()
        {
            var lastHurt = owner().getLastHurtMob();
            this.ownerLastHurt = lastHurt;

            return lastHurt != null
                    && this.canAttack(lastHurt, TargetingConditions.DEFAULT)
                    && ownerLastHurtTimestamp != owner().getLastHurtMobTimestamp();
        }

        private LivingEntity ownerLastHurt;

        private int ownerLastHurtTimestamp;

        @Override
        public void start()
        {
            super.start();

            System.out.println("Set new target to " + this.ownerLastHurt);
            ownerLastHurtTimestamp = this.owner().getLastHurtMobTimestamp();
            this.thisEntity.setTarget(null, EntityTargetEvent.TargetReason.CUSTOM, true);
            this.thisEntity.setTarget(this.ownerLastHurt, EntityTargetEvent.TargetReason.CUSTOM, true);
        }
    }
}
