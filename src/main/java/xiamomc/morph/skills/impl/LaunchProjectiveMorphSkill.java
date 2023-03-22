package xiamomc.morph.skills.impl;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.network.MorphClientHandler;
import xiamomc.morph.network.commands.S2C.S2CSetAggressiveCommand;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.skills.options.ProjectiveConfiguration;
import xiamomc.morph.storage.skill.SkillConfiguration;
import xiamomc.morph.utilities.EntityTypeUtils;
import xiamomc.pluginbase.Annotations.Resolved;

public class LaunchProjectiveMorphSkill extends DelayedMorphSkill<ProjectiveConfiguration>
{
    @Override
    protected int getExecuteDelay(SkillConfiguration configuration, ProjectiveConfiguration option)
    {
        return option.executeDelay;
    }

    @Resolved
    private MorphClientHandler clientHandler;

    @Override
    protected ExecuteResult preExecute(Player player, DisguiseState state, SkillConfiguration configuration, ProjectiveConfiguration option)
    {
        if (option == null || configuration == null)
        {
            printErrorMessage(player, configuration + " doesn't seems to have a valid projective configuration");
            return ExecuteResult.fail(10);
        }

        var type = EntityTypeUtils.fromString(option.getName(), true);

        if (type == null)
        {
            printErrorMessage(player, "Invalid projective entity for configuration " + configuration.getIdentifier());
            return ExecuteResult.fail(10);
        }

        playSoundToNearbyPlayers(player, option.getSoundDistance(),
                Key.key(option.getPreLaunchSoundName()), Sound.Source.HOSTILE);

        if (state.getEntityType() == EntityType.GHAST)
        {
            state.getDisguise().setAggresive(true);
            clientHandler.sendClientCommand(player, new S2CSetAggressiveCommand(true));
        }

        return super.preExecute(player, state, configuration, option);
    }

    @Override
    protected void executeDelayedSkill(Player player, DisguiseState state, SkillConfiguration configuration, ProjectiveConfiguration option)
    {
        if (state.getEntityType() == EntityType.GHAST)
        {
            state.getDisguise().setAggresive(false);
            clientHandler.sendClientCommand(player, new S2CSetAggressiveCommand(false));
        }

        var type = EntityTypeUtils.fromString(option.getName(), true);

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

                return;
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
