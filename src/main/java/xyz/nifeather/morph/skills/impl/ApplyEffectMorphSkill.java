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
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.api.morphs.skills.SkillNames;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.SkillStrings;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.ExecutionErrorException;
import xyz.nifeather.morph.skills.MorphSkill;
import xyz.nifeather.morph.skills.options.EffectConfiguration;

public class ApplyEffectMorphSkill extends MorphSkill<EffectConfiguration>
{
    @Override
    public ISkillAbilityOptionHandler<EffectConfiguration> optionHandler()
    {
        return EffectConfiguration.OPTION_HANDLER;
    }

    @Override
    public int executeSkill(Player player, DisguiseState state, EffectConfiguration option) throws ExecutionErrorException
    {
        if (option == null)
            throw ExecutionErrorException.forMethod("executeSkill")
                    .withMessage("%s does not have a potion effect set".formatted(state.getDisguiseIdentifier()))
                    .create();

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
            throw ExecutionErrorException.forMethod("executeSkill")
                    .withMessage("An effect set for %s is invalid!".formatted(state.getDisguiseIdentifier()))
                    .create();
        }

        players.forEach(p ->
        {
            p.addPotionEffect(effect);
            p.playSound(sound);

            if (option.showGuardian())
                p.spawnParticle(Particle.ELDER_GUARDIAN, p.getLocation(), 1);
        });

        player.playSound(sound);
        return 0;
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
}
