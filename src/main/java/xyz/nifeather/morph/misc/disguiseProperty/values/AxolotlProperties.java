package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AxolotlProperties extends BaseLivingEntityProperties<Axolotl>
{
    private final Map<String, Axolotl.Variant> variantMap = new ConcurrentHashMap<>();

    private void initVariantMap()
    {
        for (Axolotl.Variant variant : Axolotl.Variant.values())
            variantMap.put(variant.name().toLowerCase(), variant);
    }

    public final SingleProperty<Axolotl.Variant> VARIANT = getSingle(PropertyNames.AXOLOTL_VARIANT, Axolotl.Variant.LUCY, this::readAxolotlVariant)
            .withRandom(Axolotl.Variant.values());

    public Optional<Axolotl.Variant> readAxolotlVariant(String input) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(Axolotl.Variant.values(), input);
    }

    public AxolotlProperties()
    {
        initVariantMap();
        VARIANT.withValidInput(variantMap.keySet());

        registerSingle(VARIANT);
    }

    @Override
    protected @Nullable Axolotl tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Axolotl axolotl ? axolotl : null;
    }

    @Override
    public void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Axolotl axolotl)
    {
        propertyHandler.set(VARIANT, axolotl.getVariant());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        propertyHandler.set(VARIANT, DisguiseUtils.pick(VARIANT.getRandomValues()));
    }

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);
        map.put(VARIANT.id(), propertyHandler.get(VARIANT).name().toLowerCase());
    }
}
