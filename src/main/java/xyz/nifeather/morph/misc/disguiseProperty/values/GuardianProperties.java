package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Guardian;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.InputHandles;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyNames;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Map;

public class GuardianProperties extends BaseLivingEntityProperties<Guardian>
{
    public final SingleProperty<Integer> ATTACK_TARGET = SingleProperty.of(PropertyNames.GUARDIAN_ATTACK_TARGET, -1, InputHandles::reservedException, true);

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
    protected void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull Guardian targetEntity)
    {
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

    @Override
    protected void appendNetworkMap(PropertyHandler propertyHandler, Map<String, String> map)
    {
        super.appendNetworkMap(propertyHandler, map);

        map.put(ATTACK_TARGET.id(), propertyHandler.getOptional(ATTACK_TARGET).orElse(0) + "");
    }
}
