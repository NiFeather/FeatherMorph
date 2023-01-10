package xiamomc.morph.skills.impl;

import net.minecraft.world.damagesource.DamageSource;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.skills.MorphSkill;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.storage.skill.ExplosionConfiguration;
import xiamomc.morph.storage.skill.SkillConfiguration;

public class ExplodeMorphSkill extends MorphSkill<ExplosionConfiguration>
{
    @Override
    public int executeSkill(Player player, SkillConfiguration configuration, ExplosionConfiguration option)
    {
        if (option == null)
        {
            printErrorMessage(player, configuration + "的爆炸设置无效");
            return 10;
        }

        var strength = option.getStrength();
        var setsFire = option.setsFire();
        var killsSelf = option.killsSelf();

        if (!player.getWorld().createExplosion(player, strength, setsFire,
                Boolean.TRUE.equals(player.getWorld().getGameRuleValue(GameRule.MOB_GRIEFING))))
        {
            sendDenyMessageToPlayer(player, SkillStrings.explodeFailString()
                    .withLocale(MessageUtils.getLocale(player))
                    .toComponent(null));

            return 20;
        }

        if (killsSelf && !(player.getGameMode() == GameMode.CREATIVE))
        {
            var nmsPlayer = ((CraftPlayer) player).getHandle();

            nmsPlayer.hurt(DamageSource.explosion(nmsPlayer, null), 1);
            player.setHealth(0);
        }

        return configuration.getCooldown();
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillType.EXPLODE;
    }

    private final ExplosionConfiguration option = new ExplosionConfiguration();

    @Override
    public ExplosionConfiguration getOption()
    {
        return option;
    }
}
