package xyz.nifeather.morph.skills.impl;

import org.bukkit.NamespacedKey;
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
import xyz.nifeather.morph.misc.ExecutionErrorException;
import xyz.nifeather.morph.skills.MorphSkill;
import xyz.nifeather.morph.skills.options.NoOpConfiguration;

import java.util.concurrent.ThreadLocalRandom;

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
     * @param player 玩家
     * @param state  {@link DisguiseState}
     * @param option 此技能的详细设置
     * @return 执行后的冷却长度
     */
    @Override
    public int executeSkill(Player player, DisguiseState state, NoOpConfiguration option) throws ExecutionErrorException
    {
        var launchedProjectile = launchProjectile(player, EntityType.SPLASH_POTION, 0.4f);

        if (launchedProjectile == null)
        {
            throw ExecutionErrorException.forMethod("executeSkill")
                    .withMessage("Unable to spawn entity")
                    .create();
        }

        if (!(launchedProjectile instanceof ThrownPotion thrownPotion))
        {
            throw ExecutionErrorException.forMethod("executeSkill")
                    .withMessage("Error summoning splash potion: Excepted ThrownPotion, but got %s".formatted(launchedProjectile.getClass()))
                    .create();
        }

        var meta = thrownPotion.getPotionMeta();
        var info = validTypes[ThreadLocalRandom.current().nextInt(0, validTypes.length)];
        var potionEffect = new PotionEffect(info.type, info.duration, info.amplifier, false, true);
        meta.addCustomEffect(potionEffect, true);
        meta.setColor(info.type().getColor());

        thrownPotion.setPotionMeta(meta);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITCH_THROW, 1, 1);
        return 0;
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
