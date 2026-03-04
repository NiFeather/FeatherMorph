package xyz.nifeather.morph.utilities;

import ca.spottedleaf.moonrise.common.util.TickThread;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NmsUtils
{
    private static final Logger log = LoggerFactory.getLogger(NmsUtils.class);

    public static Entity spawnEntity(EntityType bukkitType, World targetWorld, Location location)
    {
        var nmsType = EntityTypeUtils.getNmsType(bukkitType);

        if (nmsType == null)
            throw new IllegalArgumentException("No NMS EntityType for bukkit type '%s'".formatted(bukkitType));

        var nmsWorld = ((CraftWorld) targetWorld).getHandle();
        var nmsEntity = nmsType.create(nmsWorld, EntitySpawnReason.COMMAND);

        if (nmsEntity == null)
            throw new IllegalArgumentException("Unable to spawn entity");

        nmsEntity.setPos(new Vec3(location.x(), location.y(), location.z()));

        return nmsEntity.getBukkitEntity();
    }

    public static ServerLevel getNmsLevel(World world)
    {
        return ((CraftWorld)world).getHandle();
    }

    private final static Map<EntityType, List<String>> syncableAttributesMap = new ConcurrentHashMap<>();

    public static List<AttributeInstance> getValidAttributes(EntityType targetType, AttributeMap mapToLookup)
    {
        var validAttributes = getSyncableAttributeListFor(targetType);

        if (validAttributes.isEmpty()) return new ObjectArrayList<>();

        var existing = new ObjectArrayList<>(mapToLookup.getSyncableAttributes());
        existing.removeIf(instance -> validAttributes.stream().noneMatch(s -> s.equals(instance.getAttribute().getRegisteredName())));

        return existing;
    }

    @Unmodifiable
    public static List<String> getSyncableAttributeListFor(EntityType bukkitType)
    {
        var cached = syncableAttributesMap.getOrDefault(bukkitType, null);
        if (cached != null)
            return cached;

        if (!bukkitType.isAlive())
            return List.of();

        var nmsType = EntityTypeUtils.getNmsType(bukkitType);

        if (nmsType == null || !DefaultAttributes.hasSupplier(nmsType))
            return List.of();

        var supplier = DefaultAttributes.getSupplier((net.minecraft.world.entity.EntityType<? extends LivingEntity>) nmsType);

        List<String> validAttributes = new ObjectArrayList<>();

        Map<?, ?> instances = ReflectionUtils.getValue(supplier, "instances", Map.class);
        for (Map.Entry<?, ?> entry : instances.entrySet())
        {
            var value = entry.getValue();

            if (!(value instanceof AttributeInstance nmsAttributeInstance))
                continue;

            var holder = (Holder<Attribute>) entry.getKey();

            if (!nmsAttributeInstance.getAttribute().value().isClientSyncable())
                continue;

            var identifier = BuiltInRegistries.ATTRIBUTE.getKey(holder.value());
            if (identifier == null) continue;

            validAttributes.add(identifier.toString());
        }

        syncableAttributesMap.put(bukkitType, Collections.synchronizedList(validAttributes));

        return validAttributes;
    }

    public static boolean isTickThreadFor(Entity entity)
    {
        var nms = ((CraftEntity) entity).getHandle();

        return TickThread.isTickThreadFor(nms);
    }
}
