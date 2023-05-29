package xiamomc.morph.abilities.impl;

import org.bukkit.FluidCollisionMode;
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
        if (player.isSneaking())
        {
            var direction = player.getEyeLocation().getDirection().normalize();

            var boundingBox = NmsRecord.ofPlayer(player).getBoundingBox().inflate(0.1f);

            var distance = boundingBox.getXsize() / 2 + 0.51f;

            //获取方块
            var hitResult = player.getWorld().rayTraceBlocks(player.getLocation(),
                            new Vector(direction.getX(), 0, direction.getZ()),
                            distance,
                            FluidCollisionMode.NEVER);

            //第一遍没有，翻转180度再试一遍
            if (hitResult == null)
                hitResult = player.getWorld().rayTraceBlocks(player.getLocation(),
                        new Vector(-direction.getX(), 0, -direction.getZ()),
                        distance,
                        FluidCollisionMode.NEVER);

            if (hitResult == null) return true;

            var block = hitResult.getHitBlock();
            if (block == null) return true;

            //检查是否有和方块碰撞
            var blockBox = block.getBoundingBox();
            boolean intersects = boundingBox.intersects(
                    blockBox.getMinX(),
                    blockBox.getMinY(),
                    blockBox.getMinZ(),

                    blockBox.getMaxX(),
                    blockBox.getMaxY(),
                    blockBox.getMaxZ()
            );

            if (intersects)
                player.setVelocity(new Vector(0, 0.1f, 0));
        }

        return true;
    }
}
