package xyz.nifeather.morph.skills.impl;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.WitherSkull;
import org.jetbrains.annotations.NotNull;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.api.morphs.skills.SkillNames;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.SkillStrings;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.network.commands.S2C.set.S2CSetAggressiveCommand;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.skills.options.ProjectileConfiguration;

public class LaunchProjectileMorphSkill extends DelayedMorphSkill<ProjectileConfiguration>
{
    @Override
    public ISkillAbilityOptionHandler<ProjectileConfiguration> optionHandler()
    {
        return ProjectileConfiguration.OPTION_HANDLER;
    }

    @Override
    protected int getExecuteDelay(ProjectileConfiguration option)
    {
        return option.executeDelay;
    }

    @Resolved
    private MorphClientHandler clientHandler;

    @Override
    protected ExecuteResult preExecute(Player player, DisguiseState state, @NotNull ProjectileConfiguration option)
    {
        playSoundToNearbyPlayers(player, option.getSoundDistance(),
                Key.key(option.getPreLaunchSoundName()), Sound.Source.HOSTILE);

        state.getDisguiseWrapper().setAggressive(true);
        clientHandler.sendCommand(player, new S2CSetAggressiveCommand(true));

        return ExecuteResult.success(0);
    }

    @Override
    protected void executeDelayedSkill(Player player, DisguiseState state, ProjectileConfiguration option)
    {
        state.getDisguiseWrapper().setAggressive(false);
        clientHandler.sendCommand(player, new S2CSetAggressiveCommand(false));

        var type = option.entityType();

        Entity target = null;
        var distanceLimit = option.getDistanceLimit();

        if (distanceLimit > 0)
        {
            target = player.getTargetEntity(distanceLimit);

            if (target == null)
            {
                sendDenyMessageToPlayer(player, SkillStrings.noTargetString().resolve("distance", "" + distanceLimit));
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
        return SkillNames.LAUNCH_PROJECTILE;
    }
}
