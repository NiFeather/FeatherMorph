package xiamomc.morph.skills;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.*;

public class BlazeMorphSkill extends MorphSkill
{
    @Override
    public int executeSkill(Player player)
    {
        launchProjectile(player, EntityType.SMALL_FIREBALL);

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
