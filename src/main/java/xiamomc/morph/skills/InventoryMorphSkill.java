package xiamomc.morph.skills;

import org.bukkit.entity.Player;
import xiamomc.morph.MorphManager;
import xiamomc.morph.messages.MessageUtils;
import xiamomc.morph.messages.SkillStrings;
import xiamomc.pluginbase.Annotations.Resolved;

public abstract class InventoryMorphSkill extends MorphSkill
{
    @Resolved
    private MorphManager manager;

    @Override
    public int executeSkill(Player player)
    {
        var state = manager.getDisguiseStateFor(player);
        assert state != null;

        var defaultShown = state.toggleDisguisedItems();

        manager.spawnParticle(player, player.getLocation(), player.getWidth(), player.getHeight(), player.getWidth());

        player.sendMessage(MessageUtils.prefixes(player, defaultShown
                ? SkillStrings.displayingDisguiseInventoryString
                : SkillStrings.displayingPlayerInventoryString));

        return 20;
    }
}
