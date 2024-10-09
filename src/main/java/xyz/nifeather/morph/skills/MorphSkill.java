package xyz.nifeather.morph.skills;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.Wall;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.SkillStrings;
import xyz.nifeather.morph.storage.skill.ISkillOption;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.List;

public abstract class MorphSkill<T extends ISkillOption> extends MorphPluginObject implements IMorphSkill<T>
{
    protected void playSoundToNearbyPlayers(Player player, int distance, Key key, Sound.Source source)
    {
        var loc = player.getLocation();

        //volume需要根据距离判断
        var sound = Sound.sound(key, source, distance / 8f, 1f);

        player.getWorld().playSound(sound, loc.getX(), loc.getY(), loc.getZ());
    }

    protected List<Player> findNearbyPlayers(Player player, int distance)
    {
        return DisguiseUtils.findNearbyPlayers(player, distance, false);
    }

    protected void sendDenyMessageToPlayer(Player player, Component text)
    {
        player.sendMessage(MessageUtils.prefixes(player, text.color(NamedTextColor.RED)));

        player.playSound(Sound.sound(Key.key("minecraft", "entity.villager.no"),
                Sound.Source.PLAYER, 1f, 1f));
    }

    protected void printErrorMessage(Player player, String message)
    {
        logger.error(message);

        sendDenyMessageToPlayer(player, SkillStrings.exceptionOccurredString()
                .withLocale(MessageUtils.getLocale(player)).toComponent(null));
    }

    /**
     * 向玩家的目标方向发射实体
     * @param player 玩家
     * @param fireballType 实体
     * @return 发射的实体，如果为null则发射失败
     * @param <E> 发射出去的实体
     */
    @Nullable
    protected <E extends Entity> E launchProjectile(Player player, EntityType fireballType, float multiplier)
    {
        Entity fireBall;
        try
        {
            fireBall = player.getWorld()
                    .spawnEntity(player.getEyeLocation(), fireballType, CreatureSpawnEvent.SpawnReason.CUSTOM);
        }
        catch (Throwable t)
        {
            printErrorMessage(player, "Unable to summon " + fireballType + ": " + t.getMessage());
            t.printStackTrace();
            return null;
        }

        if (fireBall instanceof Projectile projectile)
            projectile.setShooter(player);

        // It works for all previous MC versions.
        // Then starting from 1.20 we need to multiply 0.1 for most projectiles right before we do any other things.
        // However, the Thrown potions still keep the 1.19 behavior
        //
        // Then starting 1.21, we no longer requires to multiply 0.1 again.
        //
        // Why?

        double extraMultiplier = switch (fireBall)
        {
            case ThrownPotion thrownPotion -> 2.25d;
            case ThrowableProjectile throwableProjectile -> 1.4d;
            case LlamaSpit llamaSpit -> 1.4d;
            default -> 1d;
        };

        var velocity = player.getEyeLocation()
                .getDirection()
                .normalize()
                .multiply(extraMultiplier);

        fireBall.setVelocity(velocity.multiply(multiplier));

        return (E) fireBall;
    }

    protected float getTopY(Block block)
    {
        var data = block.getBlockData();

        var val = block.getBoundingBox().getMinY() + block.getBoundingBox().getHeight();

        if (data instanceof Fence || data instanceof Wall || data instanceof Gate)
            val += 0.5d;

        return (float) Math.max(block.getLocation().getY(), val);
    }
}
