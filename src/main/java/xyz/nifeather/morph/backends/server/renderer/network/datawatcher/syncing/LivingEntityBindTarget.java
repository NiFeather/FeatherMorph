package xyz.nifeather.morph.backends.server.renderer.network.datawatcher.syncing;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.MainHand;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.joml.Vector3i;
import xyz.nifeather.morph.backends.server.renderer.utilties.WatcherUtils;
import xyz.nifeather.morph.misc.NmsRecord;
import xyz.nifeather.morph.utilities.FoliaThreadUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class LivingEntityBindTarget implements IBindTarget
{
    private final LivingEntity bindingLiving;

    public LivingEntityBindTarget(LivingEntity living)
    {
        this.bindingLiving = living;
    }

    /**
     * Whether this target is still in the world
     */
    @Override
    public boolean isActive()
    {
        return bindingLiving.isValid();
    }

    @Override
    public EntityType entityType()
    {
        return EntityType.PLAYER;
    }

    @Override
    public float health()
    {
        return (float) bindingLiving.getHealth();
    }

    @Override
    public Collection<AttributeInstance> syncableAttributes()
    {
        var bukkitRegistry = Registry.ATTRIBUTE;

        return NmsRecord.ofLiving(bindingLiving).getAttributes().getSyncableAttributes()
                .stream().map(i ->
                {
                    var key = BuiltInRegistries.ATTRIBUTE.getKey(i.getAttribute().value());
                    if (key == null) return null;

                    var bukkitKey = NamespacedKey.fromString(key.toString());
                    if (bukkitKey == null)
                        throw new IllegalArgumentException("Invalid Bukkit key for NMS key '%s', is the server broked?".formatted(key.toString()));

                    return bindingLiving.getAttribute(bukkitRegistry.getOrThrow(bukkitKey));
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Passengers of this target
     */
    @Override
    public List<Integer> passengers()
    {
        return List.of();
    }

    /**
     * The vehicle, if any.
     */
    @Override
    public Optional<Integer> vehicle()
    {
        return Optional.empty();
    }

    /**
     * Get the location of this bind target
     */
    @Override
    public Location location()
    {
        return bindingLiving.getLocation();
    }

    /**
     * See {@link org.bukkit.entity.Entity#getYHeadRot()}
     */
    @Override
    public float yHeadRotation()
    {
        return NmsRecord.ofLiving(bindingLiving).getYHeadRot();
    }

    /**
     * Get players that's viewing this target
     */
    @Override
    public List<Player> viewingPlayers()
    {
        return WatcherUtils.getAffectedPlayers(location(), p -> p.equals(bindingLiving));
    }

    /**
     * Get the current equipment of this target
     */
    @Override
    public EntityEquipment equipment()
    {
        return bindingLiving.getEquipment();
    }

    /**
     * Get the Motion (Or velocity) of this target
     */
    @Override
    public Vector motion()
    {
        return bindingLiving.getVelocity();
    }

    @Override
    public String name()
    {
        return bindingLiving.getName();
    }

    @Override
    public Component nameComponent()
    {
        return bindingLiving.name();
    }

    @Override
    public byte dataFlags()
    {
        byte bitMask = 0x00;
        if (bindingLiving.getFireTicks() > 0 || bindingLiving.getVisualFire() == TriState.TRUE)
            bitMask |= (byte) 0x01;

        if (bindingLiving.isSneaking())
            bitMask |= (byte) 0x02;

        if (NmsRecord.ofLiving(bindingLiving).isSprinting())
            bitMask |= (byte) 0x08;

        if (bindingLiving.isSwimming())
            bitMask |= (byte) 0x10;

        if (bindingLiving.isInvisible())
            bitMask |= (byte) 0x20;

        if (bindingLiving.isGlowing())
            bitMask |= (byte) 0x40;

        if (NmsRecord.ofLiving(bindingLiving).isFallFlying())
            bitMask |= (byte) 0x80;

        return bitMask;
    }

    /**
     * See {@link LivingEntity#DATA_LIVING_ENTITY_FLAGS}
     *
     * @return
     */
    @Override
    public byte livingEntityFlags()
    {
        byte flagBit = 0x00;

        var nmsLiving = NmsRecord.ofLiving(bindingLiving);

        if (nmsLiving.isUsingItem())
        {
            flagBit |= 0x01;

            var handInUse = nmsLiving.getUsedItemHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;

            boolean isOffhand = handInUse == EquipmentSlot.OFF_HAND;
            if (isOffhand) flagBit |= 0x02;
        }

        if (bindingLiving.isRiptiding())
            flagBit |= 0x04;

        return flagBit;
    }

    @Override
    public Pose pose()
    {
        return bindingLiving.getPose();
    }

    @Override
    public int freezeTicks()
    {
        return bindingLiving.getFreezeTicks();
    }

    @Override
    public boolean hasGravity()
    {
        return bindingLiving.hasGravity();
    }

    @Override
    public Collection<PotionEffect> activePotionEffects()
    {
        return bindingLiving.getActivePotionEffects();
    }

    @Override
    public int arrowsInBody()
    {
        return bindingLiving.getArrowsInBody();
    }

    @Override
    public int beeStingersInBody()
    {
        return bindingLiving.getBeeStingersInBody();
    }

    @Override
    public Optional<Vector3i> sleepingPos()
    {
        if (!bindingLiving.isSleeping())
            return Optional.empty();

        var nmsLiving = NmsRecord.ofLiving(bindingLiving);

        var bedLocationOptional = nmsLiving.getSleepingPos();
        if (!bedLocationOptional.isPresent())
            return Optional.empty();

        var bedLocation = bedLocationOptional.get();

        return Optional.of(new Vector3i(bedLocation.getX(), bedLocation.getY(), bedLocation.getZ()));
    }

    /**
     * @return Skin flags for player disguise
     */
    @Override
    public byte skinFlags()
    {
        return (byte) 127;
    }

    /**
     * @return The main hand for the disguise
     */
    @Override
    public MainHand mainHand()
    {
        return MainHand.RIGHT;
    }

    /**
     * Run task on this target's thread, so that others can have access without the risk of async operation.
     *
     * @param func The action to run
     * @return A CompletableFuture, which may return the value returned by the function
     */
    @Override
    public <X, TBindTarget extends IBindTarget> CompletableFuture<X> runAsync(Function<TBindTarget, X> func)
    {
        return FoliaThreadUtils.delegateEntity(bindingLiving, e -> func.apply((TBindTarget) this));
    }

    /**
     * Run task on this target's thread, and wait for the given duration.<br>
     *
     * @param func
     * @param waitTimeout
     * @return
     * @throws CancellationException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    @Override
    public <X, TBindTarget extends IBindTarget> X runSynchronously(Function<TBindTarget, X> func, Duration waitTimeout) throws CancellationException, ExecutionException, TimeoutException, InterruptedException
    {
        return FoliaThreadUtils.runOnEntitySync(bindingLiving, e -> func.apply((TBindTarget) this), waitTimeout);
    }

    @Override
    public boolean equals(Entity entity)
    {
        return bindingLiving.equals(entity);
    }
}
