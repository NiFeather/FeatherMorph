package xiamomc.morph.backends.fallback;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xiamomc.morph.MorphPlugin;
import xiamomc.morph.backends.DisguiseWrapper;
import xiamomc.morph.backends.EventWrapper;
import xiamomc.morph.backends.WrapperAttribute;
import xiamomc.morph.backends.WrapperEvent;
import xiamomc.morph.misc.DisguiseEquipment;
import xiamomc.morph.misc.DisguiseState;
import xiamomc.morph.misc.disguiseProperty.SingleProperty;
import xiamomc.morph.utilities.NbtUtils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NilWrapper extends EventWrapper<NilDisguise>
{
    public NilWrapper(@NotNull NilDisguise instance, NilBackend backend)
    {
        super(instance, backend);

        this.backend = backend;
    }

    private final NilBackend backend;

    private final DisguiseEquipment equipment = new DisguiseEquipment();

    @Override
    public void mergeCompound(CompoundTag compoundTag)
    {
        var compound = readOrDefault(WrapperAttribute.nbt, null);

        if (compound == null)
        {
            compound = WrapperAttribute.nbt.createDefault();
            write(WrapperAttribute.nbt, compound);
        }

        compound.merge(compoundTag);
        this.instance.isBaby = NbtUtils.isBabyForType(getEntityType(), compoundTag);

        if (this.getEntityType() == EntityType.MAGMA_CUBE || this.getEntityType() == EntityType.SLIME)
            resetDimensions();
    }

    @Override
    public CompoundTag getCompound()
    {
        return readOrDefault(WrapperAttribute.nbt);
    }

    private static final UUID nilUUID = UUID.fromString("0-0-0-0-0");

    /**
     * Gets network id of this disguise displayed to other players
     *
     * @return The network id of this disguise
     */
    @Override
    public int getNetworkEntityId()
    {
        return -1;
    }

    private final Map<SingleProperty<?>, Object> disguiseProperties = new ConcurrentHashMap<>();

    @Override
    public <X> void writeProperty(SingleProperty<X> property, X value)
    {
        disguiseProperties.put(property, value);
    }

    @Override
    public <X> X readProperty(SingleProperty<X> property)
    {
        return this.readPropertyOr(property, property.defaultVal());
    }

    @Override
    public <X> X readPropertyOr(SingleProperty<X> property, X defaultVal)
    {
        return (X) disguiseProperties.getOrDefault(property, defaultVal);
    }

    @Nullable
    @Override
    public <R extends Tag> R getTag(@NotNull String path, TagType<R> type)
    {
        try
        {
            var obj = readOrDefault(WrapperAttribute.nbt).get(path);

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

    private static final Logger logger = MorphPlugin.getInstance().getSLF4JLogger();

    @Override
    public EntityEquipment getFakeEquipments()
    {
        return equipment;
    }

    @Override
    public void setFakeEquipments(@NotNull EntityEquipment newEquipment)
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
        var instance = new NilWrapper(this.copyInstance(), (NilBackend) getBackend());

        this.getAttributes().forEach(instance::writeInternal);

        return instance;
    }

    public static NilWrapper fromExternal(DisguiseWrapper<?> other, NilBackend backend)
    {
        var instance = new NilWrapper(new NilDisguise(other.getEntityType()), backend);

        other.getAttributes().forEach(instance::writeInternal);

        return instance;
    }

    @Override
    public boolean isBaby()
    {
        return instance.isBaby;
    }

    @Override
    public void onPostConstructDisguise(DisguiseState state, @Nullable Entity targetEntity)
    {
    }

    @Override
    public void update(DisguiseState state, Player player)
    {
    }

    @Nullable
    private Player bindingPlayer;

    @Nullable
    public Player getBindingPlayer()
    {
        return bindingPlayer;
    }

    public void setBindingPlayer(@Nullable Player player)
    {
        this.bindingPlayer = player;
    }

    @Override
    protected <T> void onAttributeWrite(WrapperAttribute<T> attribute, T value)
    {
        if (attribute.equals(WrapperAttribute.profile))
        {
            var val = ((Optional<GameProfile>) value).orElse(null);

            callEvent(WrapperEvent.SKIN_SET, val);
            return;
        }

        if (attribute.equals(WrapperAttribute.displayFakeEquip) && getBindingPlayer() != null)
        {
            backend.getNetworkingHelper().prepareMeta(getBindingPlayer())
                    .setDisguiseEquipmentShown(Boolean.TRUE.equals(value))
                    .send();

            return;
        }

        super.onAttributeWrite(attribute, value);
    }
}
