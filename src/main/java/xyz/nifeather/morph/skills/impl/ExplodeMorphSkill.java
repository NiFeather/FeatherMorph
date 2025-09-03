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
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.api.morphs.skills.SkillNames;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.SkillStrings;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.EarlyDisposeException;
import xyz.nifeather.morph.misc.ExecutionErrorException;
import xyz.nifeather.morph.network.commands.S2C.set.S2CSetSNbtCommand;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.skills.options.ExplosionConfiguration;

public class ExplodeMorphSkill extends DelayedMorphSkill<ExplosionConfiguration>
{
    @Override
    public ISkillAbilityOptionHandler<ExplosionConfiguration> optionHandler()
    {
        return ExplosionConfiguration.OPTION_HANDLER;
    }

    @Override
    protected int getExecuteDelay(ExplosionConfiguration option)
    {
        return option.executeDelay;
    }

    @Resolved
    private MorphClientHandler clientHandler;

    @Override
    protected ExecuteResult preExecute(Player player, DisguiseState state, @NotNull ExplosionConfiguration option) throws ExecutionErrorException
    {
        if (state.getEntityType() == EntityType.CREEPER)
        {
            state.getDisguiseWrapper().setAggressive(true);
            clientHandler.sendCommand(player, new S2CSetSNbtCommand("{\"ignited\": true, \"Fuse\": 30}"));
        }

        playSoundToNearbyPlayers(player, 16,
                Key.key(option.getPrimedSound()), Sound.Source.HOSTILE);

        return ExecuteResult.success(0);
    }

    @Override
    protected void executeDelayedSkill(Player player, DisguiseState state, ExplosionConfiguration option)
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

            nmsPlayer.hurtServer(nmsPlayer.level(), source, 1);
            player.setHealth(0);
        }
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillNames.EXPLODE;
    }
}
