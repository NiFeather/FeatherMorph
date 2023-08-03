package xiamomc.morph.skills.impl;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.skills.options.NoOpConfiguration;
import xiamomc.morph.storage.skill.SkillAbilityConfiguration;
import xiamomc.morph.utilities.DamageSourceUtils;

public class SonicBoomMorphSkill extends DelayedMorphSkill<NoOpConfiguration>
{
    //1.7s + 3s
    public static int defaultCooldown = 34 + 20 * 3;

    @Override
    protected ExecuteResult preExecute(Player player, DisguiseState state, SkillAbilityConfiguration configuration, NoOpConfiguration option)
    {
        playSoundToNearbyPlayers(player, 160,
                Key.key("minecraft", "entity.warden.sonic_charge"), Sound.Source.HOSTILE);

        return super.preExecute(player, state, configuration, option);
    }

    @Override
    protected int getExecuteDelay(SkillAbilityConfiguration configuration, NoOpConfiguration option)
    {
        return 34;
    }

    @Override
    protected void executeDelayedSkill(Player player, DisguiseState state, SkillAbilityConfiguration configuration, NoOpConfiguration option)
    {
        var location = player.getEyeLocation().toVector();
        var direction = player.getEyeLocation().getDirection();

        var maxDistance = 15;

        var world = player.getWorld();

        var traceResult = player.rayTraceEntities(maxDistance, true);
        CraftLivingEntity entity = null;

        if (traceResult != null && traceResult.getHitEntity() != null && traceResult.getHitEntity() instanceof CraftLivingEntity living)
            entity = living;

        playSoundToNearbyPlayers(player, 160,
                Key.key("minecraft", "entity.warden.sonic_boom"), Sound.Source.HOSTILE);

        for (int i = 1; i < maxDistance; i++)
        {
            var locNew = location.clone().add(direction.clone().multiply(i));

            if (entity != null && entity.getLocation().distance(player.getLocation()) <= i)
            {
                var record = NmsRecord.of(player, entity);
                var nmsPlayer = record.nmsPlayer();
                var nmsEntity = (LivingEntity)record.nmsEntity();
                var sources = record.nmsWorld().damageSources();

                nmsEntity.hurt(DamageSourceUtils.toNotScalable(sources.sonicBoom(nmsPlayer)), 10.0F);

                //From SonicBoom
                double d = 0.5D * (1.0D - nmsEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                double e = 2.5D * (1.0D - nmsEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                nmsEntity.push(direction.getX() * e,
                        direction.getY() * d,
                        direction.getZ() * e, nmsPlayer); // Paper

                entity = null;
            }

            world.spawnParticle(Particle.SONIC_BOOM, locNew.getX(), locNew.getY(), locNew.getZ(), 1, null);
        }
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillType.SONIC_BOOM;
    }

    @Override
    public NoOpConfiguration getOptionInstance()
    {
        return NoOpConfiguration.instance;
    }
}
