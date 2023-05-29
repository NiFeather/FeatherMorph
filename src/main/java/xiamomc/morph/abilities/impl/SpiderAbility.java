package xiamomc.morph.abilities.impl;

import io.papermc.paper.util.CollisionUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import xiamomc.morph.abilities.AbilityType;
import xiamomc.morph.config.ConfigOption;
import xiamomc.morph.config.MorphConfigManager;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.NmsRecord;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;

public class SpiderAbility extends NoOpOptionAbility
{
    private final Bindable<Boolean> modifyBox = new Bindable<>(false);

    @Initializer
    private void load(MorphConfigManager config)
    {
        config.bind(modifyBox, ConfigOption.MODIFY_BOUNDING_BOX);
    }

    /**
     * 获取此被动技能的ID
     *
     * @return {@link NamespacedKey}
     */
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityType.SPIDER;
    }

    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        if (player.isInWater()) return true;

        var boundingBox = NmsRecord.ofPlayer(player).getBoundingBox().inflate(0.1f, 0, 0.1f);
        var level = NmsRecord.ofPlayer(player).level;

        // 检查是否存在碰撞
        var bb = CollisionUtil.getCollisionsForBlocksOrWorldBorder(level, null,
                boundingBox, null, false, false, false, true, null);

        var bbBorder = CollisionUtil.getCollisionsForBlocksOrWorldBorder(level, null,
                boundingBox, null, false, false, true, true, null);

        var hasCollision = bb || bbBorder;

        if (hasCollision)
        {
            var velocity = player.getVelocity();
            player.setVelocity(new Vector(velocity.getX(), Math.min(0.15f, velocity.getY() + 0.15f), velocity.getZ()));
        }

        return true;
    }
}
