package xyz.nifeather.morph.misc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.BoundingBox;
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

    /**
     * @throws NullDependencyException If the server doesn't have any world loaded
     */
    public void initializeMapping() throws NullDependencyException
    {
        if (!boundingBoxMap.isEmpty()) return;

        var logger = FeatherMorphMain.getInstance().getSLF4JLogger();
        logger.info("Initializing entity BoundingBox mapping");

        var defaultWorld = Bukkit.getWorlds().stream()
                .findFirst()
                .orElseThrow(() -> new NullDependencyException("No world is loaded currently"));

        var spawnLocation = new Location(defaultWorld, 0, -4096d, 0);

        try
        {
            FoliaThreadUtils.delegateLocation(spawnLocation)
                    .thenAccept(w ->
                    {
                        for (EntityType type : EntityType.values())
                        {
                            if (type == EntityType.UNKNOWN) continue;
                            if (!type.isSpawnable() || !type.isAlive()) continue;

                            var entity = w.spawnEntity(spawnLocation, type, CreatureSpawnEvent.SpawnReason.CUSTOM, Entity::remove);
                            boundingBoxMap.put(type, entity.getBoundingBox().shift(0, 4096d, 0));
                        }

                        logger.info("Finished with %s entries".formatted(boundingBoxMap.size()));
                    }).get(5000, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e)
        {
            logger.error("Can't initializing BoundingBox mapping, expect problems!", e);
        }
    }

    public BoundingBox getBoundingBoxAt(EntityType type, Location location)
    {
        return getBoundboxOptional(type).map(b -> b.clone().shift(location))
                .orElse(BoundingBox.of(location, 0.6d / 2, 1.8d / 2, 0.6d / 2d).shift(0, 0.9d, 0));
    }

    //todo: Check for Slime/Magma disguise
    public Optional<BoundingBox> getBoundboxOptional(EntityType type)
    {
        return Optional.ofNullable(getBoundingBox(type));
    }

    @Nullable
    public BoundingBox getBoundingBox(EntityType type)
    {
        var box = boundingBoxMap.getOrDefault(type, null);

        return box == null ? null : box.clone();
    }
}
