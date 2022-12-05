package xiamomc.morph.skills.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.MorphManager;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.network.MorphClientHandler;
import xiamomc.morph.skills.MorphSkill;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.storage.skill.NoOpConfiguration;
import xiamomc.morph.storage.skill.SkillConfiguration;
import xiamomc.pluginbase.Annotations.Resolved;

public class InventoryMorphSkill extends MorphSkill<NoOpConfiguration>
{
    @Resolved
    private MorphManager manager;

    @Resolved
    private MorphClientHandler clientHandler;

    @Override
    public int executeSkill(Player player, SkillConfiguration configuration, NoOpConfiguration option)
    {
        var state = manager.getDisguiseStateFor(player);
        assert state != null;

        var defaultShown = state.toggleDisguisedItems();

        manager.spawnParticle(player, player.getLocation(), player.getWidth(), player.getHeight(), player.getWidth());

        clientHandler.sendClientCommand(player, "set fake_equip " + defaultShown);

        player.sendMessage(MessageUtils.prefixes(player, defaultShown
                ? SkillStrings.displayingDisguiseInventoryString()
                : SkillStrings.displayingPlayerInventoryString()));

        return configuration.getCooldown();
    }

    @Override
    public void onInitialEquip(DisguiseState state)
    {
        clientHandler.sendClientCommand(state.getPlayer(), "set fake_equip " + state.showingDisguisedItems());

        super.onInitialEquip(state);
    }

    @Override
    public void onClientinit(DisguiseState state)
    {
        clientHandler.sendClientCommand(state.getPlayer(), "set fake_equip " + state.showingDisguisedItems());

        super.onClientinit(state);
    }

    @Override
    public void onDeEquip(DisguiseState state)
    {
        clientHandler.sendClientCommand(state.getPlayer(), "set fake_equip " + false);

        super.onDeEquip(state);
    }

    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return SkillType.INVENTORY;
    }

    private final NoOpConfiguration option = new NoOpConfiguration();

    @Override
    public NoOpConfiguration getOption()
    {
        return option;
    }
}
