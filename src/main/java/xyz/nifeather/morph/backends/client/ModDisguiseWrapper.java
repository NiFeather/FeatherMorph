package xyz.nifeather.morph.backends.client;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.backends.DisguiseWrapper;
import xyz.nifeather.morph.backends.EventWrapper;
import xyz.nifeather.morph.backends.WrapperEvent;
import xyz.nifeather.morph.backends.WrapperProperties;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.disguiseProperty.DisguiseProperties;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.NbtUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ModDisguiseWrapper extends EventWrapper<TrackingClientDisguise>
{
    private final UUID waypointUUID;

    public ModDisguiseWrapper(@NotNull TrackingClientDisguise instance, ModBackend backend)
    {
        super(instance, backend);

        this.backend = backend;
        this.waypointUUID = UUID.randomUUID();
    }

    @Override
    public @NotNull UUID getVirtualEntityUUID()
    {
        var val = readPropertyOr(DisguiseProperties.INSTANCE.offTreeProperties().VIRTUAL_ENTITY_UUID, null);
        return Objects.requireNonNull(val, "VirtualEntityUUID is not set for an instance of ModDisguiseWrapper");
    }

    private final ModBackend backend;

    @Override
    public void mergeCompound(CompoundTag compoundTag)
    {
        var compound = readPropertyOr(WrapperProperties.NBT, null);

        if (compound == null)
        {
            compound = WrapperProperties.NBT.defaultVal().copy();
            writeProperty(WrapperProperties.NBT, compound);
        }

        compound.merge(compoundTag);
        this.writeProperty(DisguiseProperties.INSTANCE.offTreeProperties().IS_BABY, NbtUtils.isBabyForType(getEntityType(), compound));

        if (this.getEntityType() == EntityType.MAGMA_CUBE || this.getEntityType() == EntityType.SLIME)
            resetDimensions();
    }

    @Override
    public CompoundTag getCompound()
    {
        return readPropertyOr(WrapperProperties.NBT, WrapperProperties.NBT.defaultVal().copy());
    }

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

    @Override
    public Map<SingleProperty<?>, Object> getProperties()
    {
        return new Object2ObjectOpenHashMap<>(this.instance.disguiseProperties());
    }

    @Override
    public <X> void writeProperty(SingleProperty<X> property, X value)
    {
        this.instance.writeProperty(property, value);

        //todo: Move this to TrackingClientDisguise
        if (property.equals(WrapperProperties.PROFILE))
        {
            var val = ((Optional<GameProfile>) value).orElse(null);

            callEvent(WrapperEvent.SKIN_SET, val);
            return;
        }

        if (property.equals(WrapperProperties.DISPLAY_FAKE_EQUIP) && getBindingPlayer() != null)
        {
            backend.getNetworkingHelper().prepareMeta(getBindingPlayer())
                    .setDisguiseEquipmentShown(Boolean.TRUE.equals(value))
                    .send();

            return;
        }
    }

    @Override
    public <X> @NotNull X readProperty(SingleProperty<X> property)
    {
        return this.instance.readProperty(property);
    }

    @Override
    public <X> X readPropertyOr(SingleProperty<X> property, X defaultVal)
    {
        return this.instance.readPropertyOr(property, defaultVal);
    }

    @Override
    public <X> X readPropertyOrThrow(SingleProperty<X> property)
    {
        return this.instance.readPropertyOrThrow(property);
    }

    @Nullable
    @Override
    public <R extends Tag> R getTag(@NotNull String path, TagType<R> type)
    {
        try
        {
            var obj = getCompound().get(path);

            if (obj != null && obj.getType().equals(type))
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

    private static final Logger logger = FeatherMorphMain.getInstance().getSLF4JLogger();

    @Override
    public EntityEquipment getFakeEquipments()
    {
        return this.instance.equipment();
    }

    @Override
    public void setFakeEquipments(@NotNull EntityEquipment newEquipment)
    {
        this.instance.equipment().setArmorContents(newEquipment.getArmorContents());

        this.instance.equipment().setHandItems(newEquipment.getItemInMainHand(), newEquipment.getItemInOffHand());
    }

    @Override
    public void setServerSelfView(boolean enabled)
    {
    }

    @Override
    public EntityType getEntityType()
    {
        return instance.entityType();
    }

    @Override
    public TrackingClientDisguise copyInstance()
    {
        return instance.clone();
    }

    @Override
    public DisguiseWrapper<TrackingClientDisguise> clone()
    {
        var newWrapper = new ModDisguiseWrapper(this.copyInstance(), (ModBackend) getBackend());

        newWrapper.instance.disguiseProperties().putAll(this.instance.disguiseProperties());

        return newWrapper;
    }

    public static ModDisguiseWrapper fromExternal(DisguiseWrapper<?> other, ModBackend backend)
    {
        var newWrapper = new ModDisguiseWrapper(new TrackingClientDisguise(other.getEntityType()), backend);

        newWrapper.instance.disguiseProperties().putAll(other.getProperties());

        return newWrapper;
    }

    @Override
    public boolean isBaby()
    {
        return this.readPropertyOr(DisguiseProperties.INSTANCE.offTreeProperties().IS_BABY, false);
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
}
