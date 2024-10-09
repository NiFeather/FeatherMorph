package xyz.nifeather.morph.abilities.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.abilities.AbilityType;
import xyz.nifeather.morph.config.ConfigOption;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.NmsRecord;
import xyz.nifeather.morph.utilities.CollisionUtils;
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
        if (player.isInWater() || player.isSneaking()) return true;

        var boundingBox = NmsRecord.ofPlayer(player).getBoundingBox().inflate(0.02f, 0, 0.02f);
        var hasCollision = CollisionUtils.hasCollisionWithBlockOrBorder(player, boundingBox);

        // 检查是否存在碰撞
        // var bb = CollisionUtil.getCollisionsForBlocksOrWorldBorder(level, null,
        //        boundingBox, new ObjectArrayList<>(), new ObjectArrayList<>(), CollisionUtil.COLLISION_FLAG_CHECK_BORDER, null);

        if (hasCollision)
        {
            var velocity = player.getVelocity();
            player.setVelocity(new Vector(velocity.getX(), Math.min(0.15f, velocity.getY() + 0.15f), velocity.getZ()));
        }

        return true;
    }
}
