package xiamomc.morph.utilities;

import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

public class NmsUtils
{
    public static Entity spawnEntity(EntityType bukkitType, World targetWorld, Location location)
    {
        var nmsType = EntityTypeUtils.getNmsType(bukkitType);

        if (nmsType == null)
            throw new IllegalArgumentException("No NMS EntityType for bukkit type '%s'".formatted(bukkitType));

        var nmsWorld = ((CraftWorld) targetWorld).getHandle();
        var nmsEntity = nmsType.create(nmsWorld);

        if (nmsEntity == null)
            throw new IllegalArgumentException("Unable to spawn entity");

        nmsEntity.setPos(new Vec3(location.x(), location.y(), location.z()));

        return nmsEntity.getBukkitEntity();
    }
}
