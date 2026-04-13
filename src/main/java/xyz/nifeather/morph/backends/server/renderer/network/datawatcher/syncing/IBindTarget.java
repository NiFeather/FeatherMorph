package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing;

import net.kyori.adventure.text.Component;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Location;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.MainHand;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.joml.Vector3i;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public interface IBindTarget
{
    /**
     * Whether this target is still in the world
     */
    boolean isActive();

    EntityType entityType();

    float health();

    Collection<AttributeInstance> syncableAttributes();

    /**
     * Passengers of this target
     */
    List<Integer> passengers();

    /**
     * The vehicle, if any.
     */
    Optional<Integer> vehicle();

    /**
     * Get the location of this bind target
     */
    Location location();

    /**
     * See {@link net.minecraft.world.entity.Entity#getYHeadRot()}
     */
    float yHeadRotation();

    /**
     * Get players that's viewing this target
     */
    List<Player> viewingPlayers();

    /**
     * Get the current equipment of this target
     */
    EntityEquipment equipment();

    /**
     * Get the Motion (Or velocity) of this target
     */
    Vector motion();

    String name();

    Component nameComponent();

    /**
     * See {@link net.minecraft.world.entity.Entity#DATA_SHARED_FLAGS_ID}
     */
    byte dataFlags();

    /**
     * See {@link LivingEntity#DATA_LIVING_ENTITY_FLAGS}
     * @return
     */
    byte livingEntityFlags();

    Pose pose();

    int freezeTicks();

    boolean hasGravity();

    Collection<PotionEffect> activePotionEffects();

    int arrowsInBody();
    int beeStingersInBody();

    Optional<Vector3i> sleepingPos();

    /**
     * @return Skin flags for player disguise
     */
    byte skinFlags();

    /**
     * @return The main hand for the disguise
     */
    MainHand mainHand();

    /**
     * Run task on this target's thread, so that others can have access without the risk of async operation.
     * @param func The action to run
     * @return A CompletableFuture, which may return the value returned by the function
     * @param <X> The return value
     * @param <TBindTarget> Mostly, this bind target
     */
    <X, TBindTarget extends IBindTarget> CompletableFuture<X> runAsync(Function<TBindTarget, X> func);

    /**
     * Run task on this target's thread, and wait for the given duration.<br>
     * @param func
     * @param waitTimeout
     * @return
     * @param <X>
     * @param <TBindTarget>
     * @throws CancellationException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    <X, TBindTarget extends IBindTarget> X runSynchronously(Function<TBindTarget, X> func, Duration waitTimeout)
            throws CancellationException, ExecutionException, TimeoutException, InterruptedException;

    boolean equals(Entity entity);
}
