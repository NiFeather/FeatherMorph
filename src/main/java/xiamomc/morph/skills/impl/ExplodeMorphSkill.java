package xiamomc.morph.skills.impl;

import me.libraryaddict.disguise.disguisetypes.watchers.CreeperWatcher;
import net.minecraft.world.damagesource.DamageSource;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.network.MorphClientHandler;
import xiamomc.morph.network.commands.S2C.S2CSetSNbtCommand;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.skills.options.ExplosionConfiguration;
import xiamomc.morph.storage.skill.SkillConfiguration;
import xiamomc.pluginbase.Annotations.Resolved;

public class ExplodeMorphSkill extends DelayedMorphSkill<ExplosionConfiguration>
{
    @Override
    protected int getExecuteDelay(SkillConfiguration configuration, ExplosionConfiguration option)
    {
        return option.executeDelay;
    }

    @Resolved
    private MorphClientHandler clientHandler;

    @Override
    protected ExecuteResult preExecute(Player player, DisguiseState state, SkillConfiguration configuration, ExplosionConfiguration option)
    {
        if (option == null)
        {
            printErrorMessage(player, configuration + " doesn't seems to have a valid explosion configuration");
            return ExecuteResult.fail(10);
        }

        if (state.getEntityType() == EntityType.CREEPER)
        {
            ((CreeperWatcher)state.getDisguise().getWatcher()).setIgnited(true);
            clientHandler.sendClientCommand(player, new S2CSetSNbtCommand("{\"ignited\": true}"));
        }

        return super.preExecute(player, state, configuration, option);
    }

    @Override
    protected void executeDelayedSkill(Player player, DisguiseState state, SkillConfiguration configuration, ExplosionConfiguration option)
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
            ((CreeperWatcher)state.getDisguise().getWatcher()).setIgnited(false);
            clientHandler.sendClientCommand(player, new S2CSetSNbtCommand("{\"ignited\": false, \"Fuse\": 0}"));
        }

        if (killsSelf && !(player.getGameMode() == GameMode.CREATIVE))
        {
            var nmsPlayer = ((CraftPlayer) player).getHandle();

            nmsPlayer.hurt(DamageSource.explosion(nmsPlayer, null), 1);
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
    public ExplosionConfiguration getOption()
    {
        return option;
    }
}
