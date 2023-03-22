package xiamomc.morph.backends;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.pluginbase.Exceptions.NullDependencyException;

public abstract class DisguiseWrapper<T>
{
    protected T instance;

    public DisguiseWrapper(@NotNull T instance)
    {
        if (instance == null)
            throw new NullDependencyException("A disguise instance cannot be null");

        this.instance = instance;
    }

    public T getInstance()
    {
        return instance;
    }

    public abstract EntityEquipment getDisplayingEquipments();
    public abstract void setDisplayingEquipments(EntityEquipment newEquipment);

    public abstract void toggleServerSelfView(boolean enabled);

    public abstract EntityType getEntityType();

    public abstract T copyInstance();

    public abstract DisguiseWrapper<T> clone();

    /**
     * 返回此伪装的名称
     * @return 伪装名称
     */
    public abstract String getDisguiseName();

    public abstract void setDisguiseName(String name);

    public boolean isPlayerDisguise()
    {
        return getEntityType() == EntityType.PLAYER;
    }

    public boolean isMobDisguise()
    {
        return getEntityType().isAlive() && getEntityType() != EntityType.PLAYER;
    }

    public abstract BoundingBox getBoundingBox();

    public abstract void setGlowingColor(ChatColor glowingColor);
    public abstract void setGlowing(boolean glowing);
    public abstract ChatColor getGlowingColor();

    public abstract void addCustomData(String key, Object data);
    public abstract Object getCustomData(String key);

    public abstract void applySkin(GameProfile profile);

    @Nullable
    public abstract GameProfile getSkin();

    public abstract void onPostConstructDisguise(DisguiseState state, @Nullable Entity targetEntity);

    public abstract String serializeDisguiseData();

    public abstract void update(boolean isClone, DisguiseState state, Player player);

    public abstract void initializeCompound(CompoundTag rootCompound);

    //region Temp

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

    public void setAggresive(boolean aggresive)
    {
    }

    //endregion
}
