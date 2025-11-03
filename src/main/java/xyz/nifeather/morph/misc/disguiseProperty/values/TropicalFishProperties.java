package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TropicalFish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.utilities.DisguiseUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TropicalFishProperties extends BaseLivingEntityProperties<TropicalFish>
{
    public final SingleProperty<DyeColor> BODY_COLOR = createProperty(PropertyNames.TROPICAL_FISH_BODY_COLOR, DyeColor.GREEN, InputHandles::readDyeColor, OutputHandles::writeEnum)
            .withRandom(DyeColor.values());
    public final SingleProperty<DyeColor> PATTERN_COLOR = createProperty(PropertyNames.TROPICAL_FISH_PATTERN_COLOR, DyeColor.BLACK, InputHandles::readDyeColor, OutputHandles::writeEnum)
            .withRandom(DyeColor.values());
    public final SingleProperty<TropicalFish.Pattern> PATTERN = createProperty(PropertyNames.TROPICAL_FISH_PATTERN, TropicalFish.Pattern.BLOCKFISH, this::readPattern, OutputHandles::writeEnum)
            .withRandom(TropicalFish.Pattern.values());

    private Optional<TropicalFish.Pattern> readPattern(String propertyName, String string) throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(TropicalFish.Pattern.values(), propertyName, string);
    }

    public TropicalFishProperties()
    {
        List<String> dyeColorInputs = Arrays.stream(DyeColor.values())
                .map(c -> c.name().toLowerCase())
                .toList();

        BODY_COLOR.withValidInput(dyeColorInputs);
        PATTERN_COLOR.withValidInput(dyeColorInputs);
        PATTERN.withValidInput(Arrays.stream(TropicalFish.Pattern.values()).map(p -> p.name().toLowerCase()).toList());

        registerSingle(BODY_COLOR, PATTERN_COLOR, PATTERN);
    }

    @Override
    protected @Nullable TropicalFish tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof TropicalFish tropicalFish ? tropicalFish : null;
    }

    @Override
    protected void setupPropertiesFromEntity(DisguiseMeta meta, PropertyHandler propertyHandler, @NotNull TropicalFish targetEntity)
    {
        super.setupPropertiesFromEntity(meta, propertyHandler, targetEntity);

        var bodyColor = targetEntity.getBodyColor();
        var patternColor = targetEntity.getPatternColor();
        var pattern = targetEntity.getPattern();

        propertyHandler.set(PATTERN_COLOR, patternColor);
        propertyHandler.set(BODY_COLOR, bodyColor);
        propertyHandler.set(PATTERN, pattern);
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
        var bodyColor = DisguiseUtils.pick(BODY_COLOR.getRandomValues());
        var patternColor = DisguiseUtils.pick(PATTERN_COLOR.getRandomValues());
        var pattern = DisguiseUtils.pick(PATTERN.getRandomValues());

        propertyHandler.set(BODY_COLOR, bodyColor);
        propertyHandler.set(PATTERN_COLOR, patternColor);
        propertyHandler.set(PATTERN, pattern);
    }

}
