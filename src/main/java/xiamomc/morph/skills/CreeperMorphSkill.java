package xiamomc.morph.skills;

import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.messages.SkillStrings;

public class CreeperMorphSkill extends MorphSkill
{
    @Override
    public int executeSkill(Player player)
    {
        if (!player.getWorld().createExplosion(player, 3,
                false, Boolean.TRUE.equals(player.getWorld().getGameRuleValue(GameRule.MOB_GRIEFING))))
        {
            sendDenyMessageToPlayer(player, SkillStrings.explodeFailString().toComponent());
            return 20;
        }

        if (!(player.getGameMode() == GameMode.CREATIVE))
            player.setHealth(0d);

        return 80;
    }

    @Override
    public EntityType getType()
    {
        return EntityType.CREEPER;
    }
}
