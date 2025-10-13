package xyz.nifeather.morph.skills;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphPluginObject;
import xyz.nifeather.morph.api.events.gameplay.PlayerExecuteSkillEvent;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.strings.MorphStrings;
import xyz.nifeather.morph.messages.strings.SkillStrings;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.ExecutionErrorException;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;
import xyz.nifeather.morph.network.commands.S2C.set.S2CSetSkillCooldownCommand;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.skills.impl.NoneMorphSkill;
import xyz.nifeather.morph.skills.options.NoOpConfiguration;
import xyz.nifeather.morph.storage.skill.ISkillAbilityOption;
import xyz.nifeather.morph.utilities.PermissionUtils;

public class SkillUpdater extends MorphPluginObject
{
    private final DisguiseState state;

    @NotNull
    private volatile ISkill<?> skill = NoneMorphSkill.instance;

    public ISkill<?> getBindingSkill()
    {
        return skill;
    }

    @NotNull
    private volatile ISkillAbilityOption skillOption = NoOpConfiguration.instance;

    private volatile long availableAfter;
    public long getAvailableAfter()
    {
        return availableAfter;
    }

    public void setAvailableAfter(long val, boolean notifyClient)
    {
        availableAfter = val;

        if (notifyClient)
            applyCooldownToClient();
    }

    public volatile int defaultSkillCooldown = 20;

    public SkillUpdater(DisguiseState bindingState)
    {
        this.state = bindingState;
    }

    public void update()
    {
    }

    public void onPlayerJoin()
    {
        this.skill.onInitialEquip(state);
    }

    public boolean executeSkillCheckPermission()
    {
        var player = state.getPlayer();
        if (!player.hasPermission(CommonPermissions.SKILL))
        {
            MessageUtils.send(player, CommandStrings.noPermissionMessage());
            return false;
        }

        if (!state.canActivateSkill())
        {
            MessageUtils.send(player, SkillStrings.skillNotAvailableString());
            return false;
        }

        if (player.getGameMode() == GameMode.SPECTATOR || skill == NoneMorphSkill.instance)
        {
            MessageUtils.send(player, SkillStrings.skillNotAvaliableString());

            player.playSound(Sound.sound(Key.key("minecraft", "entity.villager.no"),
                    Sound.Source.PLAYER, 1f, 1f));

            return false;
        }

        var singleSkillPerm = CommonPermissions.skillPermissionOf(skill.getIdentifier().asString(), state.getDisguiseIdentifier());
        var hasSkillPerm = PermissionUtils.hasPermission(player, singleSkillPerm, true);

        if (!hasSkillPerm)
        {
            MessageUtils.send(player, CommandStrings.noPermissionMessage());

            player.playSound(Sound.sound(Key.key("minecraft", "entity.villager.no"),
                    Sound.Source.PLAYER, 1f, 1f));

            state.setSkillCooldown(5, true);
            return false;
        }

        if (plugin.getCurrentTick() < availableAfter)
        {
            var delta = availableAfter - plugin.getCurrentTick();

            MessageUtils.send(player,
                    SkillStrings.skillPreparing().resolve("time", Math.round(delta / 20f))
            );

            player.playSound(Sound.sound(Key.key("minecraft", "entity.villager.no"),
                    Sound.Source.PLAYER, 1f, 1f));

            return false;
        }

        return executeSkill();
    }

    /**
     * @apiNote This doesn't check whether the player can execute(player has the permission, skill still in cooldown, etc.)<br>
     *          See {@link SkillUpdater#executeSkillCheckPermission()}
     */
    public boolean executeSkill()
    {
        var player = state.getPlayer();
        var event = new PlayerExecuteSkillEvent(player, state);

        if (!event.callEvent())
        {
            MessageUtils.send(player, MorphStrings.operationCancelledString());
            this.setCooldown(5, true);
            return false;
        }

        var skill = (ISkill<ISkillAbilityOption>) this.skill;
        int cooldown = 0;
        try
        {
            var cd = skill.executeSkill(player, state, skillOption);
            cooldown = cd > 0 ? cd : state.getDefaultSkillCooldown();
        }
        catch (ExecutionErrorException e)
        {
            logger.error("Error executing skill", e);

            MessageUtils.send(player, SkillStrings.exceptionOccurredString(), c ->
                    c.hoverEvent(HoverEvent.showText(Component.text(e.getMessage()))));

            player.playSound(Sound.sound(Key.key("minecraft", "entity.villager.no"),
                    Sound.Source.PLAYER, 1f, 1f));

            cooldown = 20;
        }

        setCooldown(cooldown, true);

        return true;
    }

    public void setCooldown(long cooldown, boolean notifyClient)
    {
        this.availableAfter = plugin.getCurrentTick() + cooldown;

        if (notifyClient)
            this.applyCooldownToClient();
    }

    public long calculateRemainingCooldown()
    {
        return Math.max(0, availableAfter - plugin.getCurrentTick());
    }

    @Resolved(shouldSolveImmediately = true)
    private MorphClientHandler clientHandler;

    public void applyCooldownToClient()
    {
        clientHandler.sendCommand(state.getPlayer(), new S2CSetSkillCooldownCommand(calculateRemainingCooldown()));
    }

    public void submitCooldown(CooldownManager cooldownManager)
    {
        cooldownManager.submit(state.getPlayerUUID(), state.getDisguiseIdentifier(), availableAfter);
    }

    public <O extends ISkillAbilityOption> void bindSkill(ISkill<O> newSkill, O option)
    {
        this.skill.onDeEquip(state);

        if (newSkill == null) newSkill = (ISkill<O>) NoneMorphSkill.instance;
        if (option == null) option = (O) NoOpConfiguration.instance;

        this.skill = newSkill;
        this.skillOption = option;

        newSkill.onInitialEquip(state);
    }
}
