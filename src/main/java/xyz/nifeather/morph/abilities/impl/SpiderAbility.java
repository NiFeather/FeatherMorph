package xyz.nifeather.morph.abilities.impl;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import xiamomc.pluginbase.Annotations.Initializer;
import xiamomc.pluginbase.Bindables.Bindable;
import xyz.nifeather.morph.api.morphs.abilities.AbilityNames;
import xyz.nifeather.morph.config.ConfigOptions;
import xyz.nifeather.morph.config.MorphConfigManager;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.utilities.CollisionUtils;

public class SpiderAbility extends NoOpOptionAbility
{
    private final Bindable<Boolean> modifyBox = new Bindable<>(false);

    @Initializer
    private void load(MorphConfigManager config)
    {
        config.bind(modifyBox, ConfigOptions.MODIFY_BOUNDING_BOX);
    }

    /**
     * 获取此被动技能的ID
     *
     * @return {@link NamespacedKey}
     */
    @Override
    public @NotNull NamespacedKey getIdentifier()
    {
        return AbilityNames.SPIDER;
    }

    @Override
    public boolean handle(Player player, DisguiseState state)
    {
        if (player.isInWater() || player.isSneaking()) return true;

        var boundingBox = player.getBoundingBox().expand(0.02f, 0f, 0.02f);
        var hasCollision = CollisionUtils.hasHardCollision(player.getWorld(), boundingBox);

        if (hasCollision)
        {
            var velocity = player.getVelocity();
            player.setVelocity(new Vector(velocity.getX(), Math.min(0.15f, velocity.getY() + 0.15f), velocity.getZ()));
        }

        return true;
    }
}
