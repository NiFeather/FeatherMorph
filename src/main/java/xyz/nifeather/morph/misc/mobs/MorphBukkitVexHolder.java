package xyz.nifeather.morph.misc.mobs;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.entity.CraftVex;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nifeather.morph.FeatherMorphMain;

import java.util.EnumSet;
import java.util.Objects;

public class MorphBukkitVexHolder
{
    private static final Logger log = LoggerFactory.getLogger(MorphBukkitVexHolder.class);
    private final Vex vex;
    private final Player owner;

    public MorphBukkitVexHolder(org.bukkit.entity.Vex bukkitVex, org.bukkit.entity.Player bukkitPlayer)
    {
        this(
                ((CraftVex)bukkitVex).getHandle(),
                ((CraftPlayer)bukkitPlayer).getHandle()
        );
    }

    public MorphBukkitVexHolder(Vex nmsVex, Player owner)
    {
        Objects.requireNonNull(nmsVex, "Null NMS Vex");
        Objects.requireNonNull(owner, "Null owner");

        this.vex = nmsVex;
        this.owner = owner;

        initVex();
        updateOnEntity();
    }

    protected void updateOnEntity()
    {
        if (this.vex.isRemoved())
            return;

        var bukkitVex = this.vex.getBukkitLivingEntity();

        if (this.vex.distanceTo(this.owner) > 24)
        {
            var bukkitOwner = this.owner.getBukkitEntity();
            bukkitVex.teleportAsync(bukkitOwner.getLocation().add(-1, 0, -1)).thenRun(() ->
            {
                vex.getMoveControl().setWantedPosition(vex.getX(), vex.getY(), vex.getZ(), 2);
            });
        }

        if (owner.isRemoved())
        {
            bukkitVex.remove();
            return;
        }

        bukkitVex.getScheduler().runDelayed(FeatherMorphMain.getInstance(), task -> updateOnEntity(), () -> {}, 2);
    }

    protected void initVex()
    {
        vex.targetSelector.removeAllGoals(goal -> true);

        vex.targetSelector.addGoal(0, new MorphOwnerHurtTargetGoal(vex, this));
        vex.targetSelector.addGoal(1, new MorphOwnerHurtByTargetGoal(vex, this));
    }

    public Player getNMSOwner()
    {
        return owner;
    }

    public Vex getNMSVex()
    {
        return this.vex;
    }

    public org.bukkit.entity.Player getOwner()
    {
        return (org.bukkit.entity.Player) owner.getBukkitEntity();
    }

    public org.bukkit.entity.Vex getVex()
    {
        return (org.bukkit.entity.Vex) this.vex.getBukkitLivingEntity();
    }

    private static class MorphOwnerHurtByTargetGoal extends TargetGoal
    {
        @NotNull
        private LivingEntity owner()
        {
            return wrapper.getNMSOwner();
        }

        private final Vex thisEntity;

        private final MorphBukkitVexHolder wrapper;

        public MorphOwnerHurtByTargetGoal(Vex thisEntity, MorphBukkitVexHolder wrapper)
        {
            super(thisEntity, false);

            this.thisEntity = thisEntity;
            this.wrapper = wrapper;
        }

        @Override
        public boolean canUse()
        {
            var owner = this.owner();

            var lastHurtBy = owner.getLastHurtByMob();
            var canUse = lastHurtBy != null
                    && this.canAttack(lastHurtBy, TargetingConditions.DEFAULT)
                    && owner.tickCount - owner.getLastHurtByMobTimestamp() < 20;

            if (canUse)
                this.targetMob = lastHurtBy;

            return canUse;
        }

        @Override
        public void start()
        {
            super.start();

            if (targetMob != null && targetMob.isRemoved())
                this.targetMob = null;

            thisEntity.getMoveControl().setWantedPosition(thisEntity.getX(), thisEntity.getY(), thisEntity.getZ(), 2);

            ((org.bukkit.entity.Vex)thisEntity.getBukkitLivingEntity()).setTarget(targetMob.getBukkitLivingEntity());
        }
    }

    private static class MorphOwnerHurtTargetGoal extends TargetGoal
    {
        @NotNull
        private LivingEntity owner()
        {
            return wrapper.getNMSOwner();
        }

        private final Vex thisEntity;

        private final MorphBukkitVexHolder wrapper;

        public MorphOwnerHurtTargetGoal(Vex thisEntity, MorphBukkitVexHolder wrapper)
        {
            super(thisEntity, false);

            Objects.requireNonNull(thisEntity, "Null Entity");
            Objects.requireNonNull(wrapper, "Null Wrapper");

            this.wrapper = wrapper;
            this.thisEntity = thisEntity;

            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse()
        {
            var lastHurt = owner().getLastHurtMob();

            var canUse = lastHurt != null
                    && this.canAttack(lastHurt, TargetingConditions.DEFAULT)
                    && owner().tickCount - owner().getLastHurtMobTimestamp() < 20;

            if (canUse)
                targetMob = lastHurt;

            return canUse;
        }

        @Override
        public void start()
        {
            super.start();

            thisEntity.getMoveControl().setWantedPosition(thisEntity.getX(), thisEntity.getY(), thisEntity.getZ(), 2);

            if (targetMob != null && targetMob.isRemoved())
                this.targetMob = null;

            ((org.bukkit.entity.Vex)thisEntity.getBukkitLivingEntity()).setTarget(targetMob.getBukkitLivingEntity());
        }
    }
}
