package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.DisguiseMeta;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Optional;

public class EnderDragonProperties extends BaseLivingEntityProperties<EnderDragon>
{
    public final SingleProperty<Integer> DRAGON_PHASE = createProperty(PropertyNames.ENDER_DRAGON_DRAGON_PHASE, 10, this::readDragonPhase, OutputHandles::writeInteger);

    private Optional<Integer> readDragonPhase(String propertyName, String str) throws ParseErrorException
    {
        // localizable message not required, since readInteger always return a value or throw ParseErrorException
        var val = InputHandles.readInteger(propertyName, str)
                .orElseThrow(() -> new ParseErrorException(propertyName, "readDragonPhase: Unable to parse dragon phase"));

        InputHandles.throwIfOutOfBounds(propertyName, val, 0, 10);

        return Optional.of(val);
    }

    public EnderDragonProperties()
    {
        registerSingle(DRAGON_PHASE);
    }

    @Override
    protected @Nullable EnderDragon tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof EnderDragon enderDragon ? enderDragon : null;
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }
}
