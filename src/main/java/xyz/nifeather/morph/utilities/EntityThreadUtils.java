package xyz.nifeather.morph.utilities;

import ca.spottedleaf.moonrise.common.util.TickThread;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.misc.EntityRetiredException;

import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class EntityThreadUtils
{
    public static int DEFAULT_WAIT_TIMEOUT =150;
    public static TimeUnit DEFAULT_WAIT_TIMEUNIT = TimeUnit.MILLISECONDS;

    public static <X> X runOnRegionSync(Location location, Supplier<X> supplier, int timeout)
            throws CancellationException, ExecutionException, TimeoutException, InterruptedException
    {
        var future = delegateRegion(location, supplier);

        return future.get(timeout, TimeUnit.MILLISECONDS);
    }

    public static <X> CompletableFuture<X> delegateRegion(Location location, Supplier<X> supplier)
    {
        var nmsWorld = ((CraftWorld) location.getWorld()).getHandle();
        Vec3 vec = new Vec3(location.x(), location.y(), location.z());

        if (TickThread.isTickThreadFor(nmsWorld, vec))
            return CompletableFuture.completedFuture(supplier.get());

        CompletableFuture<X> future = new CompletableFuture<>();
        Bukkit.getRegionScheduler().run(FeatherMorphMain.getInstance(), location, task -> future.complete(supplier.get()));

        return future;
    }

    public static <X, E extends Entity> X runOnEntitySync(E bukkitEntity, Function<E, X> func, int timeout)
            throws CancellationException, ExecutionException, TimeoutException, InterruptedException
    {
        return delegateEntity(bukkitEntity, func).get(timeout, TimeUnit.MILLISECONDS);
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
}
