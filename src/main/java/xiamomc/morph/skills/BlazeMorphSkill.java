package xiamomc.morph.skills;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;

public class BlazeMorphSkill extends MorphSkill
{
    @Override
    public int executeSkill(Player player)
    {
        shootFireBall(player, SmallFireball.class);

        playSoundToNearbyPlayers(player, 15,
                Key.key("minecraft", "entity.blaze.shoot"), Sound.Source.HOSTILE);

        return 10;
    }

    @Override
    public EntityType getType()
    {
        return EntityType.BLAZE;
    }
}
