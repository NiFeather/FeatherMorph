package xiamomc.morph.skills.impl;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.skills.MorphSkill;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.skills.configurations.SkillConfiguration;

public class ApplyEffectMorphSkill extends MorphSkill
{
    private final PotionEffect miningFatigueEffect = new PotionEffect(PotionEffectType.SLOW_DIGGING, 6000, 2);

    @Override
    public int executeSkill(Player player, SkillConfiguration config)
    {
        var effectConfig = config.getEffectConfiguration();

        if (effectConfig == null)
        {
            printErrorMessage(player, config + "没有设置药水效果");
            return 10;
        }

        if (effectConfig.acquiresWater() && !player.isInWater())
        {
            sendDenyMessageToPlayer(player, SkillStrings.notInWaterString().toComponent());
            return 20;
        }

        var players = findNearbyPlayers(player, 50);

        var sound = Sound.sound(Key.key(effectConfig.getSoundName()), Sound.Source.PLAYER, effectConfig.getSoundDistance(), 1);

        var effect = getEffect(effectConfig.getName(), effectConfig.getDuration(), effectConfig.getMultiplier());

        if (effect == null)
        {
            printErrorMessage(player, config + "设置了无效的药水效果");
            return 10;
        }

        players.forEach(p ->
        {
            p.addPotionEffect(effect);
            p.playSound(sound);

            if (effectConfig.showGuardian())
                p.spawnParticle(Particle.MOB_APPEARANCE, p.getLocation(), 1);
        });

        player.playSound(sound);

        return config.getCooldown();
    }

    private PotionEffect getEffect(String key, int duration, int multiplier)
    {
        if (key == null) return null;

        var type = PotionEffectType.getByKey(NamespacedKey.fromString(key));

        if (type == null) return null;

        return new PotionEffect(type, duration, multiplier, false);
    }

    @Override
    public Key getIdentifier()
    {
        return SkillType.APPLY_EFFECT;
    }
}
