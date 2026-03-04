package xyz.nifeather.morph.misc.attributes;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import xyz.nifeather.morph.FeatherMorphMain;
import xyz.nifeather.morph.misc.actions.BiConsumerActions;
import xyz.nifeather.morph.utilities.NmsUtils;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DisguiseAttributeHandler
{
    private final Map<NamespacedKey, MorphAttributeInstance> attributeMap = new ConcurrentHashMap<>();

    public void initializeFor(EntityType type)
    {
        NmsUtils.getSyncableAttributeListFor(type)
                .stream()
                .map(s -> Registry.ATTRIBUTE.get(Objects.requireNonNull(NamespacedKey.fromString(s))))
                .filter(Objects::nonNull)
                .forEach(this::register);
    }

    public boolean contains(Attribute attribute)
    {
        return attributeMap.containsKey(attribute);
    }

    public boolean contains(NamespacedKey attribute)
    {
        return attributeMap.keySet().stream().anyMatch(a -> a.getKey().equals(attribute));
    }

    @Nullable
    public AttributeInstance get(Attribute attribute)
    {
        return get(attribute.getKey());
    }

    /**
     * @apiNote Making changes to the returning instance won't affect values stored in this handler. To edit attributes, use {@link DisguiseAttributeHandler#editAttribute(Attribute, Consumer)}
     * @return
     */
    @Nullable
    @Unmodifiable
    public AttributeInstance get(NamespacedKey attribute)
    {
        var val = attributeMap.getOrDefault(attribute, null);
        if (val == null) return null;

        return MorphAttributeInstance.copy(val);
    }

    public void register(Attribute attribute)
    {
        if (attributeMap.containsKey(attribute.getKey())) return;

        attributeMap.put(attribute.getKey(), new MorphAttributeInstance(attribute));
    }

    private final BiConsumerActions<NamespacedKey, AttributeInstance> attributeHook = new BiConsumerActions<>();
    public void hookOnAttributeChange(BiConsumer<NamespacedKey, AttributeInstance> hook)
    {
        attributeHook.hook(hook);
    }

    /**
     * @return {@code false} if the attribute is not valid for this handler
     */
    public boolean editAttribute(Attribute attribute,
                                 Consumer<AttributeInstance> consumer)
    {
        var instance = attributeMap.getOrDefault(attribute.getKey(), null);

        if (instance == null)
            return false;

        consumer.accept(instance);

        if (instance.isDirty()) // No need to trigger calculate this time, since it will do it automatically when someone call `getValue()`
            attributeHook.invoke(Pair.of(attribute.getKey(), MorphAttributeInstance.copy(instance)));

        return true;
    }
}
