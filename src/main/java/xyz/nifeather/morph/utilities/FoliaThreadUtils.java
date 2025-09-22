package xyz.nifeather.morph.utilities;

import ca.spottedleaf.moonrise.common.util.TickThread;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.misc.EntityRetiredException;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FoliaThreadUtils
{
    public static Duration DEFAULT_WAIT_TIMEOUT = Duration.ofMillis(150);

    public static <X> X runOnRegionSync(Location location, Supplier<X> supplier, int timeout)
            throws CancellationException, ExecutionException, TimeoutException, InterruptedException
    {
        var future = delegateRegion(location, supplier);

        return future.get(timeout, TimeUnit.MILLISECONDS);
    }

    public static <X> CompletableFuture<X> delegateRegion(Location location, Supplier<X> supplier)
    {
        var nmsWorld = ((CraftWorld) location.getWorld()).getHandle();

        if (TickThread.isTickThreadFor(nmsWorld, location.x(), location.z()))
            return CompletableFuture.completedFuture(supplier.get());

        CompletableFuture<X> future = new CompletableFuture<>();
        Bukkit.getRegionScheduler().run(FeatherMorphMain.getInstance(), location, task -> future.complete(supplier.get()));

        return future;
    }

    public static <X, E extends Entity> X runOnEntitySync(E bukkitEntity, Function<E, X> func, Duration timeout)
            throws CancellationException, ExecutionException, TimeoutException, InterruptedException
    {
        return delegateEntity(bukkitEntity, func).get(timeout.getNano(), TimeUnit.NANOSECONDS);
    }

    /**
     * Returns a {@link CompletableFuture} that will be complete on the Entity's thread,
     *   as an alternative for Entity#getScheduler()
     *
     * @return A {@link CompletableFuture} that provides the state,
     * @exception EntityRetiredException The entity has died (retired)
     * @apiNote <b>In case of the entity region died on folia, you may want to set a maximum wait time!</b>
     */
    public static <X, E extends Entity> CompletableFuture<X> delegateEntity(E bukkitEntity, Function<E, X> func)
    {
        var nmsEntity = ((CraftEntity)bukkitEntity).getHandleRaw();

        if (TickThread.isTickThreadFor(nmsEntity))
            return CompletableFuture.completedFuture(func.apply(bukkitEntity));

        CompletableFuture<X> future = new CompletableFuture<>();

        bukkitEntity.getScheduler().run(FeatherMorphMain.getInstance(),
                task -> future.complete(func.apply(bukkitEntity)),
                () -> future.completeExceptionally(new EntityRetiredException())); //retired: entity removed

        return future;
    }

    public static void runAtLocationSync(Location location, Consumer<World> worldConsumer, Duration timeout)
            throws ExecutionException, InterruptedException, TimeoutException
    {
        delegateLocation(location).thenAccept(worldConsumer).get(timeout.getNano(), TimeUnit.NANOSECONDS);
    }

    public static CompletableFuture<World> delegateLocation(Location location)
    {
        var world = location.getWorld();
        var nmsWorld = ((CraftWorld)location.getWorld()).getHandle();

        // Make sure we call the method that uses block location
        if (TickThread.isTickThreadFor(nmsWorld, 0d + location.getBlockX(), 0d + location.getBlockZ()))
            return CompletableFuture.completedFuture(world);

        CompletableFuture<World> future = new CompletableFuture<>();

        Bukkit.getRegionScheduler().run(FeatherMorphMain.getInstance(),
                location,
                task -> future.complete(world));

        return future;
    }

    public static boolean isTickThreadFor(Entity bukkitEntity)
    {
        var nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
        return TickThread.isTickThreadFor(nmsEntity);
    }

    // https://docs.papermc.io/paper/dev/folia-support/#checking-for-folia
    public static boolean isFolia()
    {
        try
        {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
    }
}
