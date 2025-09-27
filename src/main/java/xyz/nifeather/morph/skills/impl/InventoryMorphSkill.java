package xyz.nifeather.morph.skills.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.abilities.ISkillAbilityOptionHandler;
import xyz.nifeather.morph.api.morphs.skills.SkillNames;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.SkillStrings;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.ExecutionErrorException;
import xyz.nifeather.morph.network.commands.S2C.set.S2CSetDisplayingFakeEquipCommand;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.skills.MorphSkill;
import xyz.nifeather.morph.skills.options.NoOpConfiguration;

public class InventoryMorphSkill extends MorphSkill<NoOpConfiguration>
{
    @Override
    public ISkillAbilityOptionHandler<NoOpConfiguration> optionHandler()
    {
        return NoOpConfiguration.OPTION_HANDLER;
    }

    @Resolved
    private MorphManager manager;

    @Resolved
    private MorphClientHandler clientHandler;

    @Override
    public int executeSkill(Player player, DisguiseState state, NoOpConfiguration option) throws ExecutionErrorException
    {
        var defaultShown = state.toggleDisguisedItems();

        manager.spawnCloudParticle(player, player.getLocation(), player.getWidth(), player.getHeight(), player.getWidth());

        if (clientHandler.getPlayerVersion(player) < 16)
            clientHandler.sendCommand(player, new S2CSetDisplayingFakeEquipCommand(defaultShown));

        player.sendMessage(MessageUtils.prefixes(player, defaultShown
                ? SkillStrings.displayingDisguiseInventoryString()
                : SkillStrings.displayingPlayerInventoryString()));

        return 0;
    }

    @Override
    public void onInitialEquip(DisguiseState state)
    {
        // Apply the disguise equip
        var targetEntity = state.getSessionData(MorphManager.SESSIONKEY_TARGET_ENTITY, Entity.class);
        if (targetEntity != null)
        {
            var provider = state.getProvider();
            var theirState = manager.getDisguiseStateFor(targetEntity);
            var disguiseMeta = manager.getDisguiseMeta(state.getDisguiseIdentifier());

            if (provider.canCloneEquipment(disguiseMeta, targetEntity, theirState))
            {
                EntityEquipment equipment = null;

                if (theirState != null)
                {
                    equipment = theirState.showingDisguisedItems()
                            ? theirState.getDisguiseEquipment()
                            : ((LivingEntity) targetEntity).getEquipment();

                }
                else
                {
                    equipment = ((LivingEntity) targetEntity).getEquipment();
                }

                state.refreshDisguiseItems(equipment);
            }
        }

        super.onInitialEquip(state);
    }

    @Override
    public void applyToClient(DisguiseState state)
    {
        var player = state.getPlayer();
        if (clientHandler.getPlayerVersion(player) < 16)
            clientHandler.sendCommand(player, new S2CSetDisplayingFakeEquipCommand(state.showingDisguisedItems()));

        super.applyToClient(state);
    }

    @Override
    public void onDeEquip(DisguiseState state)
    {
        var player = state.getPlayer();
        if (clientHandler.getPlayerVersion(player) < 16)
            clientHandler.sendCommand(player, new S2CSetDisplayingFakeEquipCommand(false));

        super.onDeEquip(state);
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillNames.FAKE_EQUIP;
    }
}
