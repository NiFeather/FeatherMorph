package xyz.nifeather.morph.skills.impl;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.api.morphs.skills.SkillNames;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.SkillStrings;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.skills.MorphSkill;
import xyz.nifeather.morph.skills.options.EffectConfiguration;
import xyz.nifeather.morph.storage.skill.SkillAbilityConfiguration;

public class ApplyEffectMorphSkill extends MorphSkill<EffectConfiguration>
{
    @Override
    public int executeSkill(Player player, DisguiseState state, SkillAbilityConfiguration configuration, EffectConfiguration option)
    {
        if (option == null || configuration == null)
        {
            logger.error("%s does not have a potion effect set".formatted(state.getDisguiseIdentifier()));
            notifyError(player);
            return 10;
        }

        if (option.acquiresWater() && !player.isInWater())
        {
            sendDenyMessageToPlayer(player, SkillStrings.notInWaterString()
                    .withLocale(MessageUtils.getLocale(player))
                    .toComponent(null));

            return 20;
        }

        var players = findNearbyPlayers(player, option.getApplyDistance());

        var sound = Sound.sound(Key.key(option.getSoundName()), Sound.Source.PLAYER, option.getSoundDistance(), 1);

        var effect = getEffect(
                option.getName(),
                option.getDuration(),
                option.getMultiplier()
        );

        if (effect == null)
        {
            logger.error("An effect set for %s is invalid!".formatted(state.getDisguiseIdentifier()));
            notifyError(player);
            return 10;
        }

        players.forEach(p ->
        {
            p.addPotionEffect(effect);
            p.playSound(sound);

            if (option.showGuardian())
                p.spawnParticle(Particle.ELDER_GUARDIAN, p.getLocation(), 1);
        });

        player.playSound(sound);

        return configuration.getCooldown();
    }

    @Nullable
    private PotionEffect getEffect(String key, int duration, int multiplier)
    {
        if (key == null) return null;

        var keyNamespaced = NamespacedKey.fromString(key);
        if (keyNamespaced == null) return null;

        var type = Registry.POTION_EFFECT_TYPE.get(keyNamespaced);

        if (type == null) return null;

        return new PotionEffect(type, duration, multiplier, false);
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillNames.APPLY_EFFECT;
    }

    private final EffectConfiguration option = new EffectConfiguration();

    @Override
    public EffectConfiguration getOptionInstance()
    {
        return option;
    }
}
