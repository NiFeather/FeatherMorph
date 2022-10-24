package xiamomc.morph.skills;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;

public class GhastMorphSkill extends MorphSkill
{
    @Override
    public int executeSkill(Player player)
    {
        launchProjectile(player, EntityType.FIREBALL);

        playSoundToNearbyPlayers(player, 35,
                Key.key("minecraft", "entity.ghast.shoot"), Sound.Source.HOSTILE);

        return 40;
    }

    @Override
    public EntityType getType()
    {
        return EntityType.GHAST;
    }
}
