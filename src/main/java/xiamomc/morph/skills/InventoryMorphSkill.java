package xiamomc.morph.skills;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import xiamomc.morph.MorphManager;
import xiamomc.morph.messages.MessageUtils;
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

        player.sendMessage(MessageUtils.prefixes(player, Component.translatable("正显示")
                .append(Component.translatable(defaultShown ? "伪装自带的" : "自己的"))
                .append(Component.translatable("盔甲和手持物"))));

        return 20;
    }
}
