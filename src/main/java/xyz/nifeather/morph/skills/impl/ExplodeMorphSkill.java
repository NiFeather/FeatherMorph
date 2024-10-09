package xyz.nifeather.morph.skills.impl;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.network.commands.S2C.set.S2CSetSNbtCommand;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.SkillStrings;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.skills.SkillType;
import xyz.nifeather.morph.skills.options.ExplosionConfiguration;
import xyz.nifeather.morph.storage.skill.SkillAbilityConfiguration;

public class ExplodeMorphSkill extends DelayedMorphSkill<ExplosionConfiguration>
{
    @Override
    protected int getExecuteDelay(SkillAbilityConfiguration configuration, ExplosionConfiguration option)
    {
        return option.executeDelay;
    }

    @Resolved
    private MorphClientHandler clientHandler;

    @Override
    protected ExecuteResult preExecute(Player player, DisguiseState state, SkillAbilityConfiguration configuration, ExplosionConfiguration option)
    {
        if (option == null)
        {
            printErrorMessage(player, configuration + " doesn't seems to have a valid explosion configuration");
            return ExecuteResult.fail(10);
        }

        if (state.getEntityType() == EntityType.CREEPER)
        {
            state.getDisguiseWrapper().setAggressive(true);
            clientHandler.sendCommand(player, new S2CSetSNbtCommand("{\"ignited\": true, \"Fuse\": 30}"));
        }

        playSoundToNearbyPlayers(player, 16,
                Key.key(option.getPrimedSound()), Sound.Source.HOSTILE);

        return super.preExecute(player, state, configuration, option);
    }

    @Override
    protected void executeDelayedSkill(Player player, DisguiseState state, SkillAbilityConfiguration configuration, ExplosionConfiguration option)
    {
        var strength = option.getStrength();
        var setsFire = option.setsFire();
        var killsSelf = option.killsSelf();

        if (!player.getWorld().createExplosion(player, strength, setsFire,
                Boolean.TRUE.equals(player.getWorld().getGameRuleValue(GameRule.MOB_GRIEFING))))
        {
            sendDenyMessageToPlayer(player, SkillStrings.explodeFailString()
                    .withLocale(MessageUtils.getLocale(player))
                    .toComponent(null));

            return;
        }

        if (state.getEntityType() == EntityType.CREEPER)
        {
            state.getDisguiseWrapper().setAggressive(false);
            clientHandler.sendCommand(player, new S2CSetSNbtCommand("{\"ignited\": false, \"Fuse\": 0}"));
        }

        if (killsSelf && !(player.getGameMode() == GameMode.CREATIVE))
        {
            var nmsPlayer = ((CraftPlayer) player).getHandle();
            var source = ((CraftWorld) player.getWorld()).getHandle().damageSources().explosion(nmsPlayer, null);

            nmsPlayer.hurt(source, 1);
            player.setHealth(0);
        }
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillType.EXPLODE;
    }

    private final ExplosionConfiguration option = new ExplosionConfiguration();

    @Override
    public ExplosionConfiguration getOptionInstance()
    {
        return option;
    }
}
