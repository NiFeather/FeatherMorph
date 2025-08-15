package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.DyeColor;
import org.bukkit.craftbukkit.entity.CraftTropicalFish;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TropicalFish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Map;

public class TropicalFishProperties extends BaseLivingEntityProperties<TropicalFish>
{
    public final SingleProperty<Integer> VARIANT = getSingle("tropical_fish_variant", 0);

    public TropicalFishProperties()
    {
        registerSingle(VARIANT);
    }

    @Override
    protected @Nullable TropicalFish tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof TropicalFish tropicalFish ? tropicalFish : null;
    }

    @Override
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull TropicalFish targetEntity)
    {
        var bodyColor = targetEntity.getBodyColor();
        var patternColor = targetEntity.getPatternColor();
        var pattern = targetEntity.getPattern();

        // todo: We might want to replace this CraftBukkit call
        int packed = CraftTropicalFish.getData(patternColor, bodyColor, pattern);

        propertyHandler.set(VARIANT, packed);
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        var bodyColor = DisguiseUtils.pick(DyeColor.values());
        var patternColor = DisguiseUtils.pick(DyeColor.values());
        var pattern = DisguiseUtils.pick(TropicalFish.Pattern.values());

        int packed = CraftTropicalFish.getData(patternColor, bodyColor, pattern);

        propertyHandler.set(VARIANT, packed);
    }

    @Override
    public Map<String, String> mapToNetworkProperties(PropertyHandler propertyHandler)
    {
        return Map.of(
                "variant", propertyHandler.get(VARIANT).toString()
        );
    }
}
