package xiamomc.morph.utilities;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class CollisionUtils
{
    public static boolean hasCollisionWithBlockOrBorder(Entity bukkitEntity, AABB box)
    {
        var nmsEntity = ((CraftEntity)bukkitEntity).getHandle();
        var level = nmsEntity.level();

        var noBlockCollision = level.noBlockCollision(nmsEntity, box);

        var borderShape = level.getWorldBorder().getCollisionShape();
        var collidesWithBorder = Shapes.joinIsNotEmpty(borderShape, Shapes.create(box), BooleanOp.AND);

        return !noBlockCollision || collidesWithBorder;
    }
}
