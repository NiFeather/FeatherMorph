package xyz.nifeather.morph.misc;

import net.minecraft.world.phys.AABB;

/**
 * 一个含有碰撞箱数据的记录，防止外部插件因没有添加NMS依赖而无法获取伪装碰撞箱
 */
public record CollisionBoxRecord(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
{
    public double width()
    {
        return maxX - minX;
    }

    public double height()
    {
        return maxZ - minZ;
    }

    public static CollisionBoxRecord fromAABB(AABB nmsBox)
    {
        return new CollisionBoxRecord(nmsBox.minX, nmsBox.minY, nmsBox.minX, nmsBox.maxX, nmsBox.maxY, nmsBox.maxZ);
    }
}
