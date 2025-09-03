package xyz.nifeather.morph.skills.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
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

        clientHandler.sendCommand(player, new S2CSetDisplayingFakeEquipCommand(defaultShown));

        player.sendMessage(MessageUtils.prefixes(player, defaultShown
                ? SkillStrings.displayingDisguiseInventoryString()
                : SkillStrings.displayingPlayerInventoryString()));

        return 0;
    }

    @Override
    public void onInitialEquip(DisguiseState state)
    {
        state.setShowingDisguisedItems(state.showingDisguisedItems());

        super.onInitialEquip(state);
    }

    @Override
    public void applyToClient(DisguiseState state)
    {
        clientHandler.sendCommand(state.getPlayer(), new S2CSetDisplayingFakeEquipCommand(state.showingDisguisedItems()));

        super.applyToClient(state);
    }

    @Override
    public void onDeEquip(DisguiseState state)
    {
        clientHandler.sendCommand(state.getPlayer(), new S2CSetDisplayingFakeEquipCommand(false));

        super.onDeEquip(state);
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillNames.FAKE_EQUIP;
    }
}
