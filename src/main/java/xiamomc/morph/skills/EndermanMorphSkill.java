package xiamomc.morph.skills;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import xiamomc.morph.messages.SkillStrings;

public class EndermanMorphSkill extends MorphSkill
{
    @Override
    public int executeSkill(Player player)
    {
        var targetBlock = player.getTargetBlock(32);
        if (targetBlock == null || targetBlock.getBlockData().getMaterial().isAir())
        {
            sendDenyMessageToPlayer(player, SkillStrings.targetNotSuitableString().toComponent());
            return 20;
        }

        //获取位置
        var loc = targetBlock.getLocation();
        loc.setY(loc.getY() + 1);
        loc.setDirection(player.getEyeLocation().getDirection());

        playSoundToNearbyPlayers(player, 10,
                Key.key("minecraft", "entity.enderman.teleport"), Sound.Source.HOSTILE);

        player.teleport(loc);

        playSoundToNearbyPlayers(player, 10,
                Key.key("minecraft", "entity.enderman.teleport"), Sound.Source.HOSTILE);

        return 40;
    }

    @Override
    public EntityType getType()
    {
        return EntityType.ENDERMAN;
    }
}
