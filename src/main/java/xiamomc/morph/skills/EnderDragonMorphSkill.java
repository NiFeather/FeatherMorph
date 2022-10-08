package xiamomc.morph.skills;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class EnderDragonMorphSkill extends MorphSkill
{
    @Override
    public int executeSkill(Player player)
    {
        shootFireBall(player, DragonFireball.class);

        playSoundToNearbyPlayers(player, 80,
                Key.key("minecraft", "entity.ender_dragon.shoot"), Sound.Source.HOSTILE);

        return 100;
    }

    @Override
    public EntityType getType()
    {
        return EntityType.ENDER_DRAGON;
    }
}
