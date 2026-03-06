package xyz.nifeather.morph.utilities;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AttributeUtils
{
    private static final Map<EntityType, List<Attribute>> cachedAttributes = new ConcurrentHashMap<>();

    public static List<Attribute> syncableAttributesFor(EntityType entityType)
    {
        var existing = cachedAttributes.getOrDefault(entityType, null);
        if (existing != null) return existing;

        var nmsType = EntityTypeUtils.getNmsType(entityType);

        if (nmsType == null || !DefaultAttributes.hasSupplier(nmsType))
            return List.of();

        var supplier = DefaultAttributes.getSupplier((net.minecraft.world.entity.EntityType<? extends LivingEntity>) nmsType);

        Map<?, ?> instances = ReflectionUtils.getValue(supplier, "instances", Map.class);
        List<Attribute> attributes = new CopyOnWriteArrayList<>();

        // Holder<Attribute> <-> AttributeInstance
        for (Map.Entry<?, ?> entry : instances.entrySet())
        {
            var value = entry.getValue();

            if (!(value instanceof AttributeInstance nmsAttributeInstance))
                continue;

            if (!nmsAttributeInstance.getAttribute().value().isClientSyncable())
                continue;

            var holder = (Holder<net.minecraft.world.entity.ai.attributes.Attribute>) entry.getKey();

            var identifier = BuiltInRegistries.ATTRIBUTE.getKey(holder.value());
            if (identifier == null) continue;

            var bukkitKey = NamespacedKey.fromString(identifier.toString());
            Objects.requireNonNull(bukkitKey, "A NMS Identifier '%s' has a value that Bukkit's NamespacedKey doesn't accept, bad server implementation?".formatted(identifier));

            var bukkitAttribute = Registry.ATTRIBUTE.get(bukkitKey);
            if (bukkitAttribute == null) continue;

            attributes.add(bukkitAttribute);
        }

        cachedAttributes.put(entityType, attributes);
        return attributes;
    }
}
