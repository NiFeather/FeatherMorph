package xiamomc.morph.backends;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.AABB;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.misc.CollisionBoxRecord;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.utilities.EntityTypeUtils;

/**
 * A wrapper that holds the underlying disguise instance
 * @param <TInstance> Type of the disguise instance
 */
public abstract class DisguiseWrapper<TInstance>
{
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
     * Gets current displaying equipment
     * @return A {@link EntityEquipment} that presents the current displaying equipment
     */
    public abstract EntityEquipment getDisplayingEquipments();

    /**
     * Sets displaying equipment to the giving value
     * @param newEquipment A {@link EntityEquipment} that presents the new equipment
     */
    public abstract void setDisplayingEquipments(@NotNull EntityEquipment newEquipment);

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
     * Clone this wrapper
     * @return A new wrapper cloned from this instance, everything in the new instance should not have any reference with this wrapper
     */
    public abstract DisguiseWrapper<TInstance> clone();

    /**
     * 返回此伪装的名称
     * @return 伪装名称
     */
    public abstract String getDisguiseName();

    /**
     * 设置此伪装的名称
     * @param name 要设置的伪装名称
     */
    public abstract void setDisguiseName(String name);

    /**
     * Checks whether the underlying disguise is a player disguise
     * @return A value that presents whether the underlying disguise is a player disguise
     */
    public boolean isPlayerDisguise()
    {
        return getEntityType() == EntityType.PLAYER;
    }

    /**
     * Checks whether the underlying disguise is a mob disguise
     * @return A value that presents whether the underlying disguise is a mob disguise (e.g. Not a player disguise)
     */
    public boolean isMobDisguise()
    {
        return getEntityType().isAlive() && getEntityType() != EntityType.PLAYER;
    }

    /**
     * Gets a {@link AABB} matching the current disguise at an exact position
     * @return A {@link AABB} matching the current disguise at the exact position
     * @apiNote This doesn't check whether bounding box modification is enabled and will always present value from the modified one.
     */
    public AABB getBoundingBoxAt(double x, double y, double z)
    {
        return this.getDimensions().makeBoundingBox(x, y, z);
    }

    /**
     * Alternative method of {@link DisguiseWrapper#getBoundingBoxAt(double, double, double)}
     * <br>
     * Can be used if a plugin doesn't have an NMS dependency set.
     * @apiNote This doesn't check whether bounding box modification is enabled and will always present value from the modified one.
     */
    public CollisionBoxRecord getBoundingBoxAtAlternative(double x, double y, double z)
    {
        return CollisionBoxRecord.fromAABB(getBoundingBoxAt(x, y, z));
    }

    /**
     * Gets the excepted eye height for the bounding box of this disguise.
     * @apiNote This doesn't check whether bounding box modification is enabled and will always present value from the modified one.
     */
    public double getExceptingEyeHeight()
    {
        return getDimensions().height * 0.85;
    }

    private EntityDimensions dimensions;

    /**
     * 重置此Wrapper已缓存的Dimensions
     */
    protected void resetDimensions()
    {
        this.dimensions = null;
    }

    private void ensureDimensionPresent()
    {
        if (dimensions != null) return;

        // 2023/5/5: ItemDisplayProvider
        if (getEntityType() == EntityType.BLOCK_DISPLAY)
        {
            this.dimensions = EntityDimensions.fixed(1, 1);
            return;
        }
        else if (getEntityType() == EntityType.ITEM_DISPLAY)
        {
            this.dimensions = EntityDimensions.fixed(0.3f, 0.3f);
            return;
        }

        var nmsType = EntityTypeUtils.getNmsType(this.getEntityType());

        if (nmsType != null)
            this.dimensions = nmsType.getDimensions();
        else
            throw new RuntimeException("Unable to get NMS type for %s".formatted(this.getEntityType()));

        if (getEntityType() != EntityType.SLIME && getEntityType() != EntityType.MAGMA_CUBE) return;

        var dimScale = getSlimeDimensionScale();
        this.dimensions = EntityDimensions.fixed(0.51F * dimScale, 0.51F * dimScale);
    }

    /**
     * Gets the dimensions of the current disguise
     * @return A value of {@link EntityDimensions} matching the current disguise
     */
    public EntityDimensions getDimensions()
    {
        ensureDimensionPresent();

        return isBaby()
                ? dimensions.scale(this.getEntityType() == EntityType.TURTLE ? 0.3F : 0.5F)
                : dimensions;
    }

    protected abstract boolean isBaby();

    protected float getSlimeDimensionScale()
    {
        return Math.max(1, getCompound().getInt("Size"));
    }

    public abstract void setGlowingColor(ChatColor glowingColor);

    public abstract ChatColor getGlowingColor();

    /**
     * @deprecated No longer used
     */
    @Deprecated
    public abstract void setGlowing(boolean glowing);

    /**
     * Adds a custom data to the underlying instance
     * @param key Name
     * @param data Value
     */
    public abstract void addCustomData(String key, Object data);

    /**
     * Gets a custom value from the underlying instance
     * @param key Name
     * @return A value matching the provided key, null if not found
     */
    @Nullable
    public abstract Object getCustomData(String key);

    /**
     * Applies a skin to the underlying player instance
     * @param profile {@link GameProfile}
     * @apiNote This shouldn't do anything if disguise entity type is not {@link EntityType#PLAYER}
     */
    public abstract void applySkin(GameProfile profile);

    /**
     * Gets current skin from the underlying player instance
     * @return {@link GameProfile}, null if not set or not available
     */
    @Nullable
    public abstract GameProfile getSkin();

    /**
     * Actions when we finished constructing disguise
     * @param state A {@link DisguiseState} that handles the current wrapper
     * @param targetEntity The targeted entity (If there is any)
     */
    public abstract void onPostConstructDisguise(DisguiseState state, @Nullable Entity targetEntity);

    /**
     * Updates the underlying disguise instance
     * @param isClone Whether this disguise is cloned from another entity or disguise
     * @param state {@link DisguiseState}
     * @param player The player who owns the provided state
     */
    public abstract void update(boolean isClone, DisguiseState state, Player player);

    /**
     * Merge NBT to the underlying instance
     * @param compound {@link CompoundTag}
     */
    public abstract void mergeCompound(CompoundTag compound);

    /**
     * Gets a value from current compound
     * @param path NBT Path
     * @param type {@link TagType}, check {@link net.minecraft.nbt.TagTypes} for more information
     * @return A NBT tag, null if not found
     */
    @Nullable
    public abstract <R extends Tag> R getTag(String path, TagType<R> type);

    /**
     * Returns a copy of the existing compound.
     */
    public abstract CompoundTag getCompound();

    /**
     * Gets network id of this disguise displayed to other players
     * @return The network id of this disguise
     */
    public abstract int getNetworkEntityId();

    /**
     * Dispose this wrapper
     */
    public void dispose()
    {
    }

    //region Temp

    public abstract void resetAmbientSoundInterval();

    public void showArms(boolean showarms)
    {
    }

    public void setSaddled(boolean saddled)
    {
    }

    public boolean isSaddled()
    {
        return false;
    }

    public void setAggressive(boolean aggressive)
    {
    }

    //endregion
}
