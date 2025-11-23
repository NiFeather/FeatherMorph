package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Guardian;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.*;

public class GuardianProperties extends BaseLivingEntityProperties<Guardian>
{
    public final SingleProperty<Integer> ATTACK_TARGET = SingleProperty.builder(PropertyNames.GUARDIAN_ATTACK_TARGET, -1)
            .withInputHandle(InputHandles::reservedException)
            .withOutputHandle(OutputHandles::writeInteger)
            .hideFromUserInput(true)
            .build();

    public GuardianProperties()
    {
        registerSingle(ATTACK_TARGET);
    }

    @Override
    protected @Nullable Guardian tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof Guardian guardian ? guardian : null;
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

}
