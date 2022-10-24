package xiamomc.morph.skills.impl;

import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.skills.MorphSkill;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.skills.configurations.SkillConfiguration;

public class ExplodeMorphSkill extends MorphSkill
{
    @Override
    public int executeSkill(Player player, SkillConfiguration configuration)
    {
        var explodeConfig = configuration.getExplosionConfiguration();

        if (explodeConfig == null)
        {
            printErrorMessage(player, configuration + "的爆炸设置无效");
            return 10;
        }

        if (!player.getWorld().createExplosion(player,
                explodeConfig.getStrength(),
                explodeConfig.setsFire(),
                Boolean.TRUE.equals(player.getWorld().getGameRuleValue(GameRule.MOB_GRIEFING))))
        {
            sendDenyMessageToPlayer(player, SkillStrings.explodeFailString().toComponent());
            return 20;
        }

        if (explodeConfig.killsSelf() && !(player.getGameMode() == GameMode.CREATIVE))
            player.setHealth(0d);

        return configuration.getCooldown();
    }

    @Override
    public SkillType getType()
    {
        return SkillType.EXPLODE;
    }
}
