package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Armadillo;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

import java.util.Arrays;
import java.util.Optional;

public class ArmadilloPropertyCollection extends BaseLivingEntityPropertyCollection<Armadillo>
{
    public final SingleProperty<Armadillo.State> STATE = SingleProperty.builder(PropertyNames.ARMADILLO_STATE, Armadillo.State.IDLE)
            .withInputHandle(this::readArmadilloState)
            .withOutputHandle(OutputHandles::writeEnum)
            .withSuggestions(Arrays.stream(Armadillo.State.values()).map(s -> s.name().toLowerCase()).toList())
            .build();

    public ArmadilloPropertyCollection()
    {
        registerSingle(STATE);
    }

    private Optional<Armadillo.State> readArmadilloState(String propertyName, String input)
            throws ParseErrorException
    {
        return InputHandles.readEnumNonNull(Armadillo.State.values(), propertyName, input);
    }

    @Override
    protected @Nullable Armadillo tryCastEntity(@org.jetbrains.annotations.Nullable Entity targetEntity)
    {
        return targetEntity instanceof Armadillo armadillo ? armadillo : null;
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }
}
