package xyz.nifeather.morph.utilities;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class AttributeUtils
{
    private static final Map<EntityType, List<Attribute>> cachedAttributes = new ConcurrentHashMap<>();

    public static List<Attribute> syncableAttributesFor(EntityType entityType)
    {
        var existing = cachedAttributes.getOrDefault(entityType, null);
        if (existing != null) return existing;

        var list = NmsUtils.getSyncableAttributeListFor(entityType)
                .stream()
                .map(id ->
                {
                    var namespaced = NamespacedKey.fromString(id);
                    if (namespaced == null) return null;

                    return Registry.ATTRIBUTE.get(namespaced);
                })
                .filter(Objects::nonNull)
                .toList();

        cachedAttributes.put(entityType, list);
        return list;
    }
}
