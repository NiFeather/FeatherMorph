package xiamomc.morph.skills;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ElderGuardianMorphSkill extends MorphSkill
{
    //who, when
    private final Map<UUID, Long> elderGuardianCoolDownTraceMap = new ConcurrentHashMap<>();

    private final PotionEffect miningFatigueEffect = new PotionEffect(PotionEffectType.SLOW_DIGGING, 6000, 2);

    private final int defaultCooldown = 1200;

    @Override
    public int executeSkill(Player player)
    {
        if (!player.isInWater())
        {
            sendDenyMessageToPlayer(player, Component.translatable("你需要在水里才能使用此技能"));
            return 20;
        }

        //对远古守卫者的技能进行额外限制
        var lastActivate = elderGuardianCoolDownTraceMap.get(player.getUniqueId());

        if (lastActivate != null && Plugin.getCurrentTick() - lastActivate < defaultCooldown)
        {
            sendDenyMessageToPlayer(player, Component.translatable("距离上次使用远古守卫者的技能不足1分钟"));
            return 20;
        }

        var players = findNearbyPlayers(player, 50);

        var sound = Sound.sound(Key.key("entity.elder_guardian.curse"), Sound.Source.HOSTILE, 50, 1);
        players.forEach(p ->
        {
            p.addPotionEffect(miningFatigueEffect);
            p.playSound(sound);
            p.spawnParticle(Particle.MOB_APPEARANCE, p.getLocation(), 1);
        });

        player.playSound(sound);

        elderGuardianCoolDownTraceMap.put(player.getUniqueId(), Plugin.getCurrentTick());

        return defaultCooldown;
    }

    @Override
    public EntityType getType()
    {
        return EntityType.ELDER_GUARDIAN;
    }
}
