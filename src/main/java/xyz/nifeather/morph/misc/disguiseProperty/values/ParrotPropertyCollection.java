package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Parrot.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ParrotPropertyCollection extends BaseLivingEntityPropertyCollection<Parrot>
{
    private final Map<String, Variant> variantMap = new ConcurrentHashMap<>();

    private void initMap()
    {
        for (var variant : Variant.values())
            variantMap.put(variant.name().toLowerCase(), variant);
    }

    public final SingleProperty<Parrot.Variant> VARIANT;

    private Optional<Variant> readVariant(String propertyName, String string) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(Variant.values(), propertyName, string);
    }

    public ParrotPropertyCollection()
    {
        initMap();
        VARIANT = SingleProperty.builder(PropertyNames.PARROT_VARIANT, Variant.RED)
                .withInputHandle(this::readVariant)
                .withOutputHandle(OutputHandles::writeEnum)
                .withRandom(Variant.values())
                .withSuggestions(variantMap.keySet())
                .build();

        registerSingle(VARIANT);
    }

    @Override
    protected @Nullable Parrot tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Parrot parrot ? parrot : null;
    }

    @Override
    public void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull Parrot targetEntity)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, targetEntity);

        propertyHandler.set(VARIANT, targetEntity.getVariant());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(VARIANT, DisguiseUtils.pick(VARIANT.randomValues()));
    }

}
