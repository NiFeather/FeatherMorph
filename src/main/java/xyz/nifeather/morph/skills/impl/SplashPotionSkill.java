package xyz.nifeather.morph.skills.impl;

import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.api.morphs.skills.SkillNames;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.skills.MorphSkill;
import xyz.nifeather.morph.skills.options.NoOpConfiguration;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;
import xyz.nifeather.morph.storage.skill.SkillAbilityConfigContainer;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class SplashPotionSkill extends MorphSkill<NoOpConfiguration>
{
    @Override
    public ISkillAbilityOptionHandler<NoOpConfiguration> optionHandler()
    {
        return NoOpConfiguration.OPTION_HANDLER;
    }

    /**
     * 执行伪装的主动技能
     *
     * @param player        玩家
     * @param state         {@link DisguiseState}
     * @param configuration 此技能的整体配置，包括ID、冷却等
     * @param option        此技能的详细设置
     * @return 执行后的冷却长度
     */
    @Override
    public int executeSkill(Player player, DisguiseState state, SkillAbilityConfigContainer configuration, NoOpConfiguration option)
    {
        var launchedProjectile = launchProjectile(player, EntityType.SPLASH_POTION, 0.4f);

        if (launchedProjectile == null)
        {
            logger.error("Error summoning splash potion: null");
            return configuration.getSkillCooldown();
        }

        if (!(launchedProjectile instanceof ThrownPotion thrownPotion))
        {
            logger.error("Error summoning splash potion: Excepted ThrownPotion, but get %s".formatted(launchedProjectile.getClass()));
            return configuration.getSkillCooldown();
        }

        var meta = thrownPotion.getPotionMeta();
        var info = validTypes[ThreadLocalRandom.current().nextInt(0, validTypes.length)];
        var potionEffect = new PotionEffect(info.type, info.duration, info.amplifier, false, true);
        meta.addCustomEffect(potionEffect, true);
        meta.setColor(info.type().getColor());

        thrownPotion.setPotionMeta(meta);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITCH_THROW, 1, 1);

        return configuration.getSkillCooldown();
    }

    private final PotionInfo[] validTypes = new PotionInfo[]
    {
            PotionInfo.of(PotionEffectType.REGENERATION, 45 * 20, 0),
            PotionInfo.of(PotionEffectType.REGENERATION, 20, 0),
            PotionInfo.of(PotionEffectType.SLOWNESS, 90 * 20, 0),
            PotionInfo.of(PotionEffectType.POISON, 45 * 20, 0),
            PotionInfo.of(PotionEffectType.WEAKNESS, 90 * 20, 0),
            PotionInfo.of(PotionEffectType.INSTANT_DAMAGE, 20, 0)
    };

    private record PotionInfo(PotionEffectType type, int duration, int amplifier)
    {
        public static PotionInfo of(PotionEffectType type, int duration, int amp)
        {
            return new PotionInfo(type, duration, amp);
        }
    }

    /**
     * 获取要应用的技能ID
     *
     * @return 技能ID
     */
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillNames.WITCH;
    }
}
