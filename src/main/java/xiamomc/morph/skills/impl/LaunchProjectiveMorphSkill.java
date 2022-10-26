package xiamomc.morph.skills.impl;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.WitherSkull;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.misc.EntityTypeUtils;
import xiamomc.morph.skills.MorphSkill;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.storage.skill.SkillConfiguration;

public class LaunchProjectiveMorphSkill extends MorphSkill
{
    @Override
    public int executeSkill(Player player, SkillConfiguration configuration)
    {
        var projectiveConfig = configuration.getProjectiveConfiguration();

        if (projectiveConfig == null)
        {
            printErrorMessage(player, configuration + "没有弹射物配置");
            return 10;
        }

        var type = EntityTypeUtils.fromString(projectiveConfig.getName(), true);

        if (type == null)
        {
            printErrorMessage(player, "没有为" + configuration + "配置要发射的实体");
            return 10;
        }

        Entity target = null;
        var distanceLimit = projectiveConfig.getDistanceLimit();

        if (distanceLimit > 0)
        {
            target = player.getTargetEntity(distanceLimit);

            if (target == null)
            {
                sendDenyMessageToPlayer(player, SkillStrings.noTargetString()
                        .resolve("distance", "" + distanceLimit).toComponent());

                return 10;
            }
        }

        var entity = launchProjectile(player, type, projectiveConfig.getVectorMultiplier());

        //region 发射后...

        if (entity instanceof ShulkerBullet bullet)
        {
            bullet.setTarget(target);
            bullet.setShooter(player);
        }

        if (entity instanceof WitherSkull skull)
        {
            var rd = (int) (Math.random() * 100) % 4;

            skull.setCharged(rd == 0);
        }

        //endregion 发射后...

        playSoundToNearbyPlayers(player, projectiveConfig.getSoundDistance(),
                Key.key(projectiveConfig.getSoundName()), Sound.Source.PLAYER);

        return configuration.getCooldown();
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillType.LAUNCH_PROJECTIVE;
    }
}
