package xiamomc.morph.abilities.impl;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.abilities.MorphAbility;
import xiamomc.morph.abilities.options.HealsFromEntityOption;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.PlayerOperationSimulator;
import xiamomc.morph.network.MorphClientHandler;
import xiamomc.morph.network.commands.S2C.S2CSetNbtCommand;
import xiamomc.morph.utilities.EntityTypeUtils;
import xiamomc.pluginbase.Annotations.Resolved;

import java.util.Map;

public class HealsFromEntityAbility extends MorphAbility<HealsFromEntityOption>
{
    /**
     * 获取此被动技能的ID
     *
     * @return {@link NamespacedKey}
     */
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.HEALS_FROM_ENTITY;
    }

    @Override
    protected HealsFromEntityOption createOption()
    {
        return new HealsFromEntityOption();
    }

    private final RandomSource random = RandomSource.create();

    @Resolved
    private MorphClientHandler clientHandler;

    private final Map<String, EntityType<?>> stringEntityTypeMap = new Object2ObjectOpenHashMap<>();

    private EntityType<?> getEntityType(String identifier)
    {
        var cache = stringEntityTypeMap.getOrDefault(identifier, null);

        if (cache != null) return cache;

        cache = EntityType.byString(identifier).orElse(null);
        stringEntityTypeMap.put(identifier, cache);

        return cache;
    }

    @Override
    public boolean revokeFromPlayer(Player player, DisguiseState state)
    {
        if (!super.revokeFromPlayer(player, state)) return false;

        state.beamTarget = null;

        return true;
    }

    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        //Validate option
        if (!super.handle(player, state)) return false;

        var option = this.getOptionFor(state);

        if (option == null || !option.isValid()) return false;

        //Find or refresh entity
        var nmsRecord = PlayerOperationSimulator.NmsRecord.of(player);

        if (state.beamTarget != null)
        {
            var entity = state.beamTarget;
            if (entity.isAlive())
            {
                var maxHealth = player.getMaxHealth();
                var playerHealth = player.getHealth();

                if (playerHealth > 0 && playerHealth / maxHealth < option.maxPercentage)
                    player.setHealth(Math.min(maxHealth, playerHealth + option.healAmount));
            }
            else
            {
                var lastDamageCause = entity.getBukkitEntity().getLastDamageCause();

                if (lastDamageCause instanceof EntityDamageByEntityEvent entityDamageByEntityEvent)
                {
                    var damager = ((CraftEntity)entityDamageByEntityEvent.getDamager()).getHandle();

                    if (entityDamageByEntityEvent.getDamager() instanceof Projectile projectile)
                    {
                        var source = projectile.getShooter();

                        if (source instanceof CraftEntity craftEntity)
                            damager = craftEntity.getHandle();
                    }

                    var source = entity.getType() == EntityType.END_CRYSTAL
                            ? ExplosionClass.explosion(entity, damager)
                            : ExplosionClass.magic(entity, damager);

                    nmsRecord.nmsPlayer().hurt(source, option.damageWhenDestroyed);
                }

                if (state.beamTarget == entity)
                    state.beamTarget = null;
            }
        }

        if (random.nextInt(10) == 0)
        {
            var nmsType = EntityTypeUtils.getNmsType(state.getEntityType());

            if (nmsType == null)
            {
                logger.error("No such entity type: %s".formatted(option.entityIdentifier));
                return false;
            }

            var prevEntity = state.beamTarget;

            var newEntity = findEntity(nmsRecord, nmsType, option.distance, EntityTypeUtils.fromString(option.entityIdentifier));

            if (prevEntity != newEntity)
            {
                state.beamTarget = newEntity;

                if (state.getEntityType() == org.bukkit.entity.EntityType.ENDER_DRAGON)
                    clientHandler.sendClientCommand(player, this.getBeamCommand(state));
            }
        }

        return true;
    }

    private static class ExplosionClass extends IndirectEntityDamageSource
    {
        protected ExplosionClass(String name, Entity explosion, @Nullable Entity cause)
        {
            super(name, explosion, cause);

            this.bypassArmor();
            this.bypassEnchantments();
            this.bypassMagic();
        }

        @Override
        public boolean scalesWithDifficulty()
        {
            return false;
        }

        public static ExplosionClass magic(Entity magic, Entity cause)
        {
            var source = new ExplosionClass("indirectMagic", magic, cause);
            source.setMagic();

            return source;
        }

        @NotNull
        public static ExplosionClass explosion(Entity explosion, Entity cause)
        {
            var source = new ExplosionClass("explosion.player", explosion, cause);
            source.setExplosion();

            return source;
        }

        public static ExplosionClass explosion(Entity explosion)
        {
            var source = new ExplosionClass("explosion", explosion, null);
            source.setExplosion();

            return source;
        }
    }

    @Override
    public void onClientInit(DisguiseState state)
    {
        super.onClientInit(state);

        if (state.getEntityType() == org.bukkit.entity.EntityType.ENDER_DRAGON)
            clientHandler.sendClientCommand(state.getPlayer(), getBeamCommand(state));
    }

    public S2CSetNbtCommand getBeamCommand(DisguiseState state)
    {
        var entity = state.beamTarget;

        var compound = new CompoundTag();

        compound.putInt("BeamTarget", entity != null ? entity.getId() : 0);

        return new S2CSetNbtCommand(compound);
    }

    private Entity findEntity(PlayerOperationSimulator.NmsRecord record, EntityType<?> nmsType, double expand, org.bukkit.entity.EntityType bukkitType)
    {
        try
        {
            var world = record.nmsWorld();
            var player = record.nmsPlayer();

            var boundingBox = nmsType.getDimensions()
                    .makeBoundingBox(player.position());

            Class<? extends Entity> classType = EntityTypeUtils.getNmsClass(bukkitType);

            if (classType == null) return null;

            var entities = world.getEntitiesOfClass(classType, boundingBox.inflate(expand), e ->
                    e.isAlive() && !e.isSpectator());

            Entity targetEntity = null;
            double distance = Double.MAX_VALUE;

            for (var entity : entities)
            {
                //logger.info("Looping " + entity + " for type " + nmsType.getBaseClass() + " :: " + nmsType);
                var dis = entity.distanceToSqr(player.position());

                if (dis < distance && entity.getId() != player.getId())
                {
                    targetEntity = entity;
                    distance = dis;
                }
            }

            return targetEntity;
        }
        catch (Throwable t)
        {
            logger.error("Error finding entity around player %s: %s".formatted(record.nmsPlayer(), t.getMessage()));
            t.printStackTrace();
        }

        return null;
    }
}
