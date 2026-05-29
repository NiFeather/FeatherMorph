package xyz.nifeather.morph.backends;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * A wrapper that holds the underlying disguise instance
 * @param <TInstance> Type of the disguise instance
 */
public abstract class DisguiseWrapper<TInstance>
{
    private static final Logger log = LoggerFactory.getLogger(DisguiseWrapper.class);
    protected TInstance instance;

    public DisguiseWrapper(@NotNull TInstance instance, DisguiseBackend<TInstance, ? extends DisguiseWrapper<TInstance>> backend)
    {
        this.instance = instance;
        this.backend = backend;
    }

    /**
     * Gets the underlying disguise instance
     * @return The underlying disguise instance
     */
    public TInstance getInstance()
    {
        return instance;
    }

    private final DisguiseBackend<TInstance, ? extends DisguiseWrapper<TInstance>> backend;

    public DisguiseBackend<TInstance, ? extends DisguiseWrapper<TInstance>> getBackend()
    {
        return backend;
    }

    /**
     * Sets the state of server-side SelfView for the underlying disguise instance
     * @param enabled Whether server-side SelfView should be turned on
     */
    public abstract void setServerSelfView(boolean enabled);

    /**
     * Gets current entity type for this wrapper
     * @return A value that presents the current {@link EntityType}.
     */
    public abstract EntityType getEntityType();

    /**
     * Clone the underlying disguise instance
     * @return A new instance cloned from the underlying disguise
     */
    public abstract TInstance copyInstance();

    /**
     * Called when the player exits the server
     */
    public void onPlayerOffline()
    {
    }

    /**
     * Clone this wrapper
     * @return A new wrapper cloned from this instance, everything in the new instance should not have any reference with this wrapper
     */
    public abstract DisguiseWrapper<TInstance> clone();

    /**
     * 返回此伪装的名称
     * @return 伪装名称
     */
    public String getDisguiseName()
    {
        return readProperty(WrapperProperties.DISGUISE_NAME);
    }

    /**
     * 设置此伪装的名称
     * @param name 要设置的伪装名称
     */
    public void setDisguiseName(String name)
    {
        writeProperty(WrapperProperties.DISGUISE_NAME, name);
    }

    public abstract boolean isBaby();

    /**
     * Actions when we finished constructing disguise
     * @param state A {@link DisguiseState} that handles the current wrapper
     * @param targetEntity The targeted entity (If there is any)
     */
    public abstract void postBuildDisguise(DisguiseState state, @Nullable Entity targetEntity);

    /**
     * Updates the underlying disguise instance
     */
    public abstract void update();

    /**
     * Returns a copy of the existing compound.
     */
    @Deprecated
    public abstract CompoundTag getCompound();

    /**
     * @return Empty Optional if not available
     */
    @NotNull
    public abstract UUID getVirtualEntityUUID();

    private final AtomicBoolean disposed = new AtomicBoolean(false);

    public boolean disposed()
    {
        return disposed.get();
    }

    /**
     * Dispose this wrapper
     */
    public void dispose()
    {
        disposed.set(true);
    }

    //region Temp

    public void setSaddled(boolean saddled)
    {
        writeProperty(WrapperProperties.SADDLED, saddled);
    }

    public boolean isSaddled()
    {
        return readProperty(WrapperProperties.SADDLED);
    }

    public void setAggressive(boolean aggressive)
    {
    }

    @Deprecated
    public void playAttackAnimation()
    {
    }

    //endregion

    public abstract <X> void writeProperty(SingleProperty<X> property, X value);

    /**
     * Discard a property if it has been set.<br>
     * Depending on {@link SingleProperty#restoreDefaultsBeforeDiscard()}, server-side presentation implementations may need reset the visual effect of the given property.
     *
     * @param property {@link SingleProperty} to discard
     */
    public abstract <X> void discardProperty(SingleProperty<X> property);

    /**
     * @return 与此Property对应的值，如果没有设定则返回默认值
     */
    @NotNull
    public abstract <X> X readProperty(SingleProperty<X> property);

    public abstract <X> X readPropertyOr(SingleProperty<X> property, X defaultVal);

    public abstract <X> X readPropertyOrThrow(SingleProperty<X> property);

    public abstract Map<SingleProperty<?>, Object> getProperties();

    public void onPlayerJoin(Player newInstance)
    {
    }

    public abstract <T> void subscribeEvent(Object source, WrapperEvent<T> wrapperEvent, Consumer<T> c);

    public abstract void unSubscribeEvent(Object source, WrapperEvent<?> wrapperEvent);

    public abstract void onDisguiseAttributeChange(NamespacedKey id, AttributeInstance attribute);

    public abstract void playEntityAnimation(String animateName);
    public abstract void updateEntityAnimateMask(String animateName, boolean isAllowed);
}
