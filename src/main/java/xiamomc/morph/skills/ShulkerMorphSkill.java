package xiamomc.morph.skills;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Difficulty;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;

public class ShulkerMorphSkill extends MorphSkill
{
    @Override
    public int executeSkill(Player player)
    {
        var distance = 15;
        var target = player.getTargetEntity(distance);

        if (player.getWorld().getDifficulty() == Difficulty.PEACEFUL)
        {
            sendDenyMessageToPlayer(player, Component.text("世界难度为和平"));
            return 5;
        }

        if (target != null)
        {
            var loc = player.getEyeLocation().clone();

            var bullet = player.getWorld().spawn(loc, ShulkerBullet.class);

            bullet.setTarget(target);
            bullet.setShooter(player);

            playSoundToNearbyPlayers(player, 15,
                    Key.key("minecraft", "entity.shulker.shoot"), Sound.Source.HOSTILE);
        }
        else
        {
            sendDenyMessageToPlayer(player, Component.text("视线" + distance + "格以内没有实体").color(NamedTextColor.RED));
            return 10;
        }

        return 80;
    }

    @Override
    public EntityType getType()
    {
        return EntityType.SHULKER;
    }
}
