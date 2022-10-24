package xiamomc.morph.skills.impl;

import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import xiamomc.morph.MorphManager;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.morph.skills.MorphSkill;
import xiamomc.morph.skills.SkillType;
import xiamomc.morph.skills.configurations.SkillConfiguration;
import xiamomc.pluginbase.Annotations.Resolved;

public class InventoryMorphSkill extends MorphSkill
{
    @Resolved
    private MorphManager manager;

    @Override
    public int executeSkill(Player player, SkillConfiguration configuration)
    {
        var state = manager.getDisguiseStateFor(player);
        assert state != null;

        var defaultShown = state.toggleDisguisedItems();

        manager.spawnParticle(player, player.getLocation(), player.getWidth(), player.getHeight(), player.getWidth());

        player.sendMessage(MessageUtils.prefixes(player, defaultShown
                ? SkillStrings.displayingDisguiseInventoryString()
                : SkillStrings.displayingPlayerInventoryString()));

        return configuration.getCooldown();
    }

    @Override
    public Key getIdentifier()
    {
        return SkillType.INVENTORY;
    }
}
