package xiamomc.morph.skills;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import xiamomc.morph.messages.SkillStrings;

public class WitherMorphSkill extends MorphSkill
{
    @Override
    public int executeSkill(Player player)
    {
        var skull = (WitherSkull) launchProjectile(player, EntityType.WITHER_SKULL);

        var rd = (int) (Math.random() * 100) % 4;

        skull.setCharged(rd == 0);

        playSoundToNearbyPlayers(player, 24,
                Key.key("minecraft", "entity.wither.shoot"), Sound.Source.HOSTILE);

        return 10;
    }

    @Override
    public EntityType getType()
    {
        return EntityType.WITHER;
    }
}
