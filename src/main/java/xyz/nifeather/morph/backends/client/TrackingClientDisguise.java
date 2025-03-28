package xyz.nifeather.morph.backends.client;

import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import xiamomc.pluginbase.Exceptions.NullDependencyException;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrackingClientDisguise implements Cloneable
{
    public TrackingClientDisguise(EntityType type)
    {
        this.entityType = type;
    }

    private final DisguiseEquipment equipment = new DisguiseEquipment();

    public DisguiseEquipment equipment()
    {
        return equipment;
    }

    private EntityType entityType;

    public EntityType entityType()
    {
        return entityType;
    }

    private final Map<SingleProperty<?>, Object> disguiseProperties = new ConcurrentHashMap<>();
    public Map<SingleProperty<?>, Object> disguiseProperties()
    {
        return disguiseProperties;
    }

    public <X> void writeProperty(SingleProperty<X> property, X value)
    {
        this.disguiseProperties.put(property, value);
    }

    public <X> @NotNull X readProperty(SingleProperty<X> property)
    {
        return this.readPropertyOr(property, property.defaultVal());
    }

    public <X> X readPropertyOr(SingleProperty<X> property, X defaultVal)
    {
        return (X) this.disguiseProperties.getOrDefault(property, defaultVal);
    }

    public <X> X readPropertyOrThrow(SingleProperty<X> property)
    {
        var val = this.disguiseProperties.getOrDefault(property, null);
        if (val == null) throw new NullDependencyException("The requested property '%s' was not found in %s".formatted(property.id(), this));

        return (X) val;
    }

    @Override
    protected TrackingClientDisguise clone()
    {
        TrackingClientDisguise obj;

        try
        {
            obj = (TrackingClientDisguise) super.clone();
        }
        catch (Throwable t)
        {
            obj = new TrackingClientDisguise(this.entityType());
        }

        return obj;
    }
}
