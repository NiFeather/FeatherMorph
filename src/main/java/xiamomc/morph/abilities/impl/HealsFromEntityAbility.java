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
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity;
import org.bukkit.entity.Player;
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

                if (player.getHealth() / maxHealth < option.maxPercentage)
                    player.setHealth(Math.min(maxHealth, player.getHealth() + option.healAmount));
            }
            else
            {
                var lastDamageCause = entity.getBukkitEntity().getLastDamageCause();

                if (lastDamageCause instanceof EntityDamageByEntityEvent entityDamageByEntityEvent)
                {
                    var damager = ((CraftEntity)entityDamageByEntityEvent.getDamager()).getHandle();
                    var source = ExplosionClass.create(entity, damager);
                    nmsRecord.nmsPlayer().hurt(source, 10);
                }

                if (state.beamTarget == entity)
                    state.beamTarget = null;
            }
        }

        if (random.nextInt(10) == 0)
        {
            var nmsType = getEntityType(option.entityIdentifier);

            if (nmsType == null)
            {
                logger.error("No such entity type: %s".formatted(option.entityIdentifier));
                return false;
            }

            var prevEntity = state.beamTarget;
            var newEntity = findEntity(nmsRecord, nmsType, option.distance, EntityTypeUtils.fromString(option.entityIdentifier));

            if (prevEntity != newEntity && state.getEntityType() == org.bukkit.entity.EntityType.ENDER_DRAGON)
            {
                state.beamTarget = newEntity;
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

            this.setExplosion();
        }

        public static ExplosionClass create(Entity explosion, Entity cause)
        {
            return new ExplosionClass("explosion.player", explosion, cause);
        }

        public static ExplosionClass create(Entity explosion)
        {
            return new ExplosionClass("explosion", explosion, null);
        }
    }

    @Override
    public void onClientInit(DisguiseState state)
    {
        clientHandler.sendClientCommand(state.getPlayer(), getBeamCommand(state));
        super.onClientInit(state);
    }

    public S2CSetNbtCommand getBeamCommand(DisguiseState state)
    {
        var entity = state.beamTarget;

        var compound = new CompoundTag();
        var list = new ListTag();

        if (entity != null)
        {
            list.add(0, DoubleTag.valueOf(entity.getX()));
            list.add(1, DoubleTag.valueOf(entity.getY()));
            list.add(2, DoubleTag.valueOf(entity.getZ()));
        }

        compound.put("BeamTarget", list);

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

            var entities = world.getEntitiesOfClass(classType, boundingBox.inflate(expand));

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
