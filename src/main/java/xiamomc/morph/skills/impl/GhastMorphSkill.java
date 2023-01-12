package xiamomc.morph.skills.impl;

import me.libraryaddict.disguise.disguisetypes.watchers.GhastWatcher;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.network.MorphClientHandler;
import xiamomc.morph.network.commands.S2C.S2CSetAggressiveCommand;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.storage.skill.NoOpConfiguration;
import xiamomc.morph.storage.skill.ProjectiveConfiguration;
import xiamomc.morph.storage.skill.SkillConfiguration;
import xiamomc.morph.utilities.EntityTypeUtils;
import xiamomc.pluginbase.Annotations.Resolved;

public class GhastMorphSkill extends DelayedMorphSkill<ProjectiveConfiguration>
{
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillType.GHAST;
    }

    private final ProjectiveConfiguration option = new ProjectiveConfiguration();

    @Override
    public ProjectiveConfiguration getOption()
    {
        return option;
    }

    @Resolved
    private MorphManager manager;

    @Resolved
    private MorphClientHandler clientHandler;

    @Override
    protected ExecuteResult preExecute(Player player, SkillConfiguration configuration, ProjectiveConfiguration option)
    {
        playSoundToNearbyPlayers(player, 16,
                Key.key("entity.ghast.warn"), Sound.Source.HOSTILE);

        var state = manager.getDisguiseStateFor(player);

        if (state == null || state.getEntityType() != EntityType.GHAST)
        {
            var type = state == null ? "???" : state.getEntityType().getKey().asString();
            logger.error(getIdentifier().asString() + "只能在恶魂的伪装上使用，但当前类型是" + type);
            return ExecuteResult.fail(configuration.getCooldown());
        }

        var distanceLimit = option.getDistanceLimit();

        if (distanceLimit > 0)
        {
            var target = player.getTargetEntity(distanceLimit);

            if (target == null)
            {
                sendDenyMessageToPlayer(player, SkillStrings.noTargetString()
                        .withLocale(MessageUtils.getLocale(player))
                        .resolve("distance", "" + distanceLimit)
                        .toComponent(null));

                return ExecuteResult.fail(configuration.getCooldown());
            }
        }

        var watcher = (GhastWatcher) state.getDisguise().getWatcher();
        watcher.setAggressive(true);

        clientHandler.sendClientCommand(player, new S2CSetAggressiveCommand(true));

        return super.preExecute(player, configuration, option);
    }

    public static final int executeDelay = 16;

    @Override
    protected int getExecuteDelay()
    {
        return executeDelay;
    }

    @Override
    protected void executeDelayedSkill(Player player, SkillConfiguration configuration, ProjectiveConfiguration option)
    {
        var state = manager.getDisguiseStateFor(player);
        assert state != null;

        if (state.getEntityType() != EntityType.GHAST) return;

        var watcher = (GhastWatcher) state.getDisguise().getWatcher();
        watcher.setAggressive(false);

        clientHandler.sendClientCommand(player, new S2CSetAggressiveCommand(false));

        var type = EntityTypeUtils.fromString(option.getName(), true);

        if (type != null)
        {
            playSoundToNearbyPlayers(player, option.getSoundDistance(), Key.key(option.getSoundName()), Sound.Source.HOSTILE);
            launchProjectile(player, type, 1);
        }
    }
}
