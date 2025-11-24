package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Rabbit.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RabbitPropertyCollection extends BaseLivingEntityPropertyCollection<Rabbit>
{
    private final Map<String, Type> typeMap = new ConcurrentHashMap<>();

    private void initMap()
    {
        for (var type : Type.values())
            typeMap.put(type.name().toLowerCase(), type);
    }

    public final SingleProperty<Rabbit.Type> VARIANT;

    private Optional<Type> readVariant(String propertyName, String string) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(Type.values(), propertyName, string);
    }

    public RabbitPropertyCollection()
    {
        initMap();
        VARIANT = SingleProperty.builder(PropertyNames.RABBIT_VARIANT, Type.BROWN)
                .withInputHandle(this::readVariant)
                .withOutputHandle(OutputHandles::writeEnum)
                .withRandom(Type.values())
                .withSuggestions(typeMap.keySet())
                .build();

        registerSingle(VARIANT);
    }

    @Override
    protected @Nullable Rabbit tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Rabbit rabbit ? rabbit : null;
    }

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Rabbit targetEntity)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, targetEntity);

        propertyHandler.set(VARIANT, targetEntity.getRabbitType());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(VARIANT, DisguiseUtils.pick(VARIANT.randomValues()));
    }

}
