package xiamomc.morph.skills;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Player;

public class LlamaMorphSkill extends MorphSkill
{
    @Override
    public int executeSkill(Player player)
    {
        launchProjectile(player, EntityType.LLAMA_SPIT);

        playSoundToNearbyPlayers(player, 8,
                Key.key("minecraft", "entity.llama.spit"), Sound.Source.NEUTRAL);

        return 25;
    }

    @Override
    public EntityType getType()
    {
        return EntityType.LLAMA;
    }
}
