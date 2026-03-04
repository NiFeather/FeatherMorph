package xyz.nifeather.morph.backends.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.backends.DisguiseWrapper;
import xyz.nifeather.morph.backends.EventWrapper;
import xyz.nifeather.morph.misc.DisguiseState;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.misc.disguiseProperty.values.OffTreeProperties;

import java.util.Map;
import java.util.Objects;
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
        var val = readPropertyOr(OffTreeProperties.VIRTUAL_ENTITY_UUID, null);
        return Objects.requireNonNull(val, "VirtualEntityUUID is not set for an instance of ModDisguiseWrapper");
    }

    private final ModBackend backend;

    @Override
    public Map<SingleProperty<?>, Object> getProperties()
    {
        return new Object2ObjectOpenHashMap<>(this.instance.disguiseProperties());
    }

    @Override
    public void onDisguiseAttributeChange(NamespacedKey id, AttributeInstance attribute)
    {
        // attribute feature not implemented in client renderer yet.
    }

    @Override
    public <X> void writeProperty(SingleProperty<X> property, X value)
    {
        this.instance.writeProperty(property, value);

        if (property.id().equals(PropertyNames.ENTITY_DISPLAY_DISGUISE_EQUIPMENT) && getBindingPlayer() != null)
        {
            backend.getNetworkingHelper().prepareMeta(getBindingPlayer())
                    .setDisguiseEquipmentShown(Boolean.TRUE.equals(value))
                    .send();

            return;
        }
    }

    @Override
    public <X> void discardProperty(SingleProperty<X> property, X o)
    {
        this.instance.discardProperty(property);
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

    private static final Logger logger = FeatherMorphMain.getInstance().getSLF4JLogger();

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
        //todo: implement this
        return false;
    }

    @Override
    public void postBuildDisguise(DisguiseState state, @Nullable Entity targetEntity)
    {
    }

    @Override
    public void update()
    {
    }

    @Override
    public CompoundTag getCompound()
    {
        return new CompoundTag();
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
