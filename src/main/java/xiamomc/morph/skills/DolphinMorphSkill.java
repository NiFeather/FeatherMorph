package xiamomc.morph.skills;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xiamomc.morph.messages.SkillStrings;

public class DolphinMorphSkill extends MorphSkill
{
    private final PotionEffect dolphinsGraceEffect = new PotionEffect(PotionEffectType.DOLPHINS_GRACE, 100, 0);

    @Override
    public int executeSkill(Player player)
    {
        if (!player.isInWater())
        {
            sendDenyMessageToPlayer(player, SkillStrings.notInWaterString.toComponent());
            return 5;
        }

        var players = findNearbyPlayers(player, 9);

        players.forEach(p -> p.addPotionEffect(dolphinsGraceEffect));

        return 80;
    }

    @Override
    public EntityType getType()
    {
        return EntityType.DOLPHIN;
    }
}
