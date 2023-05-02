package xiamomc.morph.backends.fallback;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.backends.DisguiseWrapper;
import xiamomc.morph.misc.DisguiseEquipment;
import xiamomc.morph.misc.DisguiseState;

public class NilWrapper extends DisguiseWrapper<NilDisguise>
{
    public NilWrapper(@NotNull NilDisguise instance)
    {
        super(instance);
    }

    private final DisguiseEquipment equipment = new DisguiseEquipment();

    @Override
    public void mergeCompound(CompoundTag compoundTag)
    {
        this.instance.compoundTag.merge(compoundTag);
    }

    @Override
    public CompoundTag getCompound()
    {
        return instance.compoundTag.copy();
    }

    @Nullable
    @Override
    public <R extends Tag> R getTag(@NotNull String path, TagType<R> type)
    {
        try
        {
            var obj = instance.compoundTag.get(path);

            if (obj != null && obj.getType() == type)
                return (R) obj;

            return null;
        }
        catch (Throwable t)
        {
            logger.error("Unable to read NBT '%s' from instance:".formatted(path));
            t.printStackTrace();

            return null;
        }
    }

    private static final Logger logger = MorphPlugin.getInstance(MorphPlugin.getMorphNameSpace()).getSLF4JLogger();

    @Override
    public EntityEquipment getDisplayingEquipments()
    {
        return equipment;
    }

    @Override
    public void setDisplayingEquipments(@NotNull EntityEquipment newEquipment)
    {
        this.equipment.setArmorContents(newEquipment.getArmorContents());

        this.equipment.setHandItems(newEquipment.getItemInMainHand(), newEquipment.getItemInOffHand());
    }

    @Override
    public void setServerSelfView(boolean enabled)
    {
    }

    @Override
    public EntityType getEntityType()
    {
        return instance.type;
    }

    @Override
    public NilDisguise copyInstance()
    {
        return instance.clone();
    }

    @Override
    public DisguiseWrapper<NilDisguise> clone()
    {
        return new NilWrapper(this.copyInstance());
    }

    /**
     * 返回此伪装的名称
     *
     * @return 伪装名称
     */
    @Override
    public String getDisguiseName()
    {
        return instance.name;
    }

    @Override
    public void setDisguiseName(String name)
    {
        this.instance.name = name;
    }

    @Deprecated
    @Override
    public BoundingBox getBoundingBox()
    {
        return new BoundingBox();
    }

    @Override
    protected float getSlimeDimensionScale()
    {
        return 4;
    }

    @Override
    public void setGlowingColor(ChatColor glowingColor)
    {
        instance.glowingColor = glowingColor;
    }

    @Override
    public void setGlowing(boolean glowing)
    {
    }

    @Override
    public ChatColor getGlowingColor()
    {
        return instance.glowingColor;
    }

    @Override
    public void addCustomData(String key, Object data)
    {
        instance.customData.put(key, data);
    }

    @Override
    public Object getCustomData(String key)
    {
        return instance.customData.getOrDefault(key, null);
    }

    @Override
    public void applySkin(GameProfile profile)
    {
        if (this.getEntityType() != EntityType.PLAYER) return;

        this.instance.profile = profile;
    }

    @Override
    public @Nullable GameProfile getSkin()
    {
        return instance.profile;
    }

    @Override
    public void onPostConstructDisguise(DisguiseState state, @Nullable Entity targetEntity)
    {
    }

    @Override
    public String serializeDisguiseData()
    {
        return "";
    }

    @Override
    public void update(boolean isClone, DisguiseState state, Player player)
    {
    }

    @Override
    public void setSaddled(boolean saddled)
    {
        instance.saddled = saddled;
    }

    @Override
    public boolean isSaddled()
    {
        return instance.saddled;
    }
}
