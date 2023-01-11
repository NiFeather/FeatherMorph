package xiamomc.morph.skills.impl;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.WitherSkull;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.utilities.EntityTypeUtils;
import xiamomc.morph.skills.MorphSkill;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.storage.skill.ProjectiveConfiguration;
import xiamomc.morph.storage.skill.SkillConfiguration;

public class LaunchProjectiveMorphSkill extends MorphSkill<ProjectiveConfiguration>
{
    @Override
    public int executeSkill(Player player, SkillConfiguration configuration, ProjectiveConfiguration option)
    {
        if (option == null || configuration == null)
        {
            printErrorMessage(player, configuration + "没有弹射物配置");
            return 10;
        }

        var type = EntityTypeUtils.fromString(option.getName(), true);

        if (type == null)
        {
            printErrorMessage(player, "没有为" + configuration + "配置要发射的实体");
            return 10;
        }

        Entity target = null;
        var distanceLimit = option.getDistanceLimit();

        if (distanceLimit > 0)
        {
            target = player.getTargetEntity(distanceLimit);

            if (target == null)
            {
                sendDenyMessageToPlayer(player, SkillStrings.noTargetString()
                        .withLocale(MessageUtils.getLocale(player))
                        .resolve("distance", "" + distanceLimit)
                        .toComponent(null));

                return 10;
            }
        }

        var entity = launchProjectile(player, type, option.getVectorMultiplier());

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

        playSoundToNearbyPlayers(player, option.getSoundDistance(),
                Key.key(option.getSoundName()), Sound.Source.PLAYER);

        return configuration.getCooldown();
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillType.LAUNCH_PROJECTIVE;
    }

    private final ProjectiveConfiguration option = new ProjectiveConfiguration();

    @Override
    public ProjectiveConfiguration getOption()
    {
        return option;
    }
}
