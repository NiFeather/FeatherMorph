package xiamomc.morph.skills.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.NetworkingHelper;
import xiamomc.morph.network.commands.S2C.clientrender.S2CRenderMapMetaCommand;
import xiamomc.morph.network.commands.S2C.clientrender.S2CRenderMeta;
import xiamomc.morph.network.commands.S2C.set.S2CSetDisplayingFakeEquipCommand;
import xiamomc.morph.network.server.MorphClientHandler;
import xiamomc.morph.skills.MorphSkill;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.skills.options.NoOpConfiguration;
import xiamomc.morph.storage.skill.SkillAbilityConfiguration;
import xiamomc.pluginbase.Annotations.Resolved;

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

    @Resolved
    private NetworkingHelper networkingHelper;

    @Override
    public void onInitialEquip(DisguiseState state)
    {
        clientHandler.sendCommand(state.getPlayer(), new S2CSetDisplayingFakeEquipCommand(state.showingDisguisedItems()));

        //发送元数据
        if (manager.isUsingNilServerBackend())
        {
            var meta = new S2CRenderMeta(state.getPlayer().getEntityId());
            meta.showOverridedEquipment = state.showingDisguisedItems();
            var packet = new S2CRenderMapMetaCommand(meta);
            networkingHelper.sendCommandToAllPlayers(packet);
        }

        super.onInitialEquip(state);
    }

    @Override
    public void onClientinit(DisguiseState state)
    {
        clientHandler.sendCommand(state.getPlayer(), new S2CSetDisplayingFakeEquipCommand(state.showingDisguisedItems()));

        super.onClientinit(state);
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
