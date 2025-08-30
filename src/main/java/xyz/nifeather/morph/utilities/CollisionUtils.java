package xyz.nifeather.morph.utilities;

import org.bukkit.World;
import org.bukkit.util.BoundingBox;

public class CollisionUtils
{
    public static boolean hasHardCollision(World world, BoundingBox boundingBox)
    {
        return world.hasCollisionsIn(boundingBox);
    }
}
