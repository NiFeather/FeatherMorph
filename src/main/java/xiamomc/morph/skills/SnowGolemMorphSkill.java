package xiamomc.morph.skills;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;

public class SnowGolemMorphSkill extends MorphSkill
{
    @Override
    public int executeSkill(Player player)
    {
        shootFireBall(player, Snowball.class);

        playSoundToNearbyPlayers(player, 8,
                Key.key("minecraft", "entity.snow_golem.shoot"), Sound.Source.NEUTRAL);

        return 15;
    }

    @Override
    public EntityType getType()
    {
        return EntityType.SNOWMAN;
    }
}
