package xyz.nifeather.morph.skills.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.network.commands.S2C.set.S2CSetDisplayingFakeEquipCommand;
import xiamomc.pluginbase.Annotations.Resolved;
import xyz.nifeather.morph.MorphManager;
import xyz.nifeather.morph.messages.MessageUtils;
import xyz.nifeather.morph.messages.SkillStrings;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.network.server.MorphClientHandler;
import xyz.nifeather.morph.skills.MorphSkill;
import xyz.nifeather.morph.skills.SkillType;
import xyz.nifeather.morph.skills.options.NoOpConfiguration;
import xyz.nifeather.morph.storage.skill.SkillAbilityConfiguration;

public class InventoryMorphSkill extends MorphSkill<NoOpConfiguration>
{
    @Resolved
    private MorphManager manager;

    @Resolved
    private MorphClientHandler clientHandler;

    @Override
    public int executeSkill(Player player, DisguiseState state, SkillAbilityConfiguration configuration, NoOpConfiguration option)
    {
        var defaultShown = state.toggleDisguisedItems();

        manager.spawnParticle(player, player.getLocation(), player.getWidth(), player.getHeight(), player.getWidth());

        clientHandler.sendCommand(player, new S2CSetDisplayingFakeEquipCommand(defaultShown));

        player.sendMessage(MessageUtils.prefixes(player, defaultShown
                ? SkillStrings.displayingDisguiseInventoryString()
                : SkillStrings.displayingPlayerInventoryString()));

        return configuration.getCooldown();
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
        return SkillType.INVENTORY;
    }

    private final NoOpConfiguration option = new NoOpConfiguration();

    @Override
    public NoOpConfiguration getOptionInstance()
    {
        return option;
    }
}
