package xyz.nifeather.morph.misc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.utilities.FoliaThreadUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Can be used for lookup common entities' default boundingbox size
 */
public class BoundingBoxLookup
{
    private static final BoundingBoxLookup instance = new BoundingBoxLookup();

    public static BoundingBoxLookup instance()
    {
        return instance;
    }

    private final Map<EntityType, BoundingBox> boundingBoxMap = new ConcurrentHashMap<>();

    @Nullable
    private BoundingBox lookupBoundingBox(EntityType type, Location executingLocation)
    {
        if (type == EntityType.UNKNOWN || !type.isAlive() || !type.isSpawnable())
            return null;

        var world = executingLocation.getWorld();
        var entity = world.spawnEntity(executingLocation.clone().add(0, -4096, 0), type, CreatureSpawnEvent.SpawnReason.CUSTOM, Entity::remove);

        var entityBox = entity.getBoundingBox();
        var box = BoundingBox.of(new Vector(0, entityBox.getHeight() / 2d, 0), entityBox.getWidthX() / 2d, entityBox.getHeight() / 2d, entityBox.getWidthZ() / 2d);
        boundingBoxMap.put(type, box);

        return box;
    }

    public BoundingBox getBoundingBoxAt(EntityType type, Location location)
    {
        return getBoundboxOptional(type, location).map(b -> b.clone().shift(location))
                .orElse(BoundingBox.of(location, 0.6d / 2, 1.8d / 2, 0.6d / 2d).shift(0, 0.9d, 0));
    }

    //todo: Check for Slime/Magma disguise
    public Optional<BoundingBox> getBoundboxOptional(EntityType type, Location executingLocation)
    {
        return Optional.ofNullable(getBoundingBox(type, executingLocation));
    }

    @Nullable
    public BoundingBox getBoundingBox(EntityType type, Location executingLocation)
    {
        var box = boundingBoxMap.getOrDefault(type, null);

        return box == null ? lookupBoundingBox(type, executingLocation) : box.clone();
    }
}
