package xyz.nifeather.morph.skills.impl;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.WitherSkull;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.network.commands.S2C.set.S2CSetAggressiveCommand;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.SkillStrings;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.skills.SkillType;
import xyz.nifeather.morph.skills.options.ProjectileConfiguration;
import xyz.nifeather.morph.storage.skill.SkillAbilityConfiguration;
import xyz.nifeather.morph.utilities.EntityTypeUtils;

public class LaunchProjectileMorphSkill extends DelayedMorphSkill<ProjectileConfiguration>
{
    @Override
    protected int getExecuteDelay(SkillAbilityConfiguration configuration, ProjectileConfiguration option)
    {
        return option.executeDelay;
    }

    @Resolved
    private MorphClientHandler clientHandler;

    @Override
    protected ExecuteResult preExecute(Player player, DisguiseState state, SkillAbilityConfiguration configuration, ProjectileConfiguration option)
    {
        if (option == null || configuration == null)
        {
            printErrorMessage(player, configuration + " doesn't seems to have a valid projective configuration");
            return ExecuteResult.fail(10);
        }

        var type = EntityTypeUtils.fromString(option.getName(), true);

        if (type == null)
        {
            printErrorMessage(player, "Invalid projective entity for " + state.getDisguiseIdentifier());
            return ExecuteResult.fail(10);
        }

        playSoundToNearbyPlayers(player, option.getSoundDistance(),
                Key.key(option.getPreLaunchSoundName()), Sound.Source.HOSTILE);

        state.getDisguiseWrapper().setAggressive(true);
        clientHandler.sendCommand(player, new S2CSetAggressiveCommand(true));

        return super.preExecute(player, state, configuration, option);
    }

    @Override
    protected void executeDelayedSkill(Player player, DisguiseState state, SkillAbilityConfiguration configuration, ProjectileConfiguration option)
    {
        state.getDisguiseWrapper().setAggressive(false);
        clientHandler.sendCommand(player, new S2CSetAggressiveCommand(false));

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
            bullet.setTarget(target);

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
        return SkillType.LAUNCH_PROJECTILE;
    }

    private final ProjectileConfiguration option = new ProjectileConfiguration();

    @Override
    public ProjectileConfiguration getOptionInstance()
    {
        return option;
    }
}
