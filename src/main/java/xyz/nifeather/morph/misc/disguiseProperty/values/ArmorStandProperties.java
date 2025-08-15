package xyz.nifeather.morph.misc.disguiseProperty.values;

import com.github.retrooper.packetevents.util.Vector3f;
import io.papermc.paper.math.Rotations;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Rotation;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.misc.disguiseProperty.PropertyHandler;
import xyz.nifeather.morph.misc.disguiseProperty.SingleProperty;

import java.util.Map;

public class ArmorStandProperties extends BaseLivingEntityProperties<ArmorStand>
{
    public final SingleProperty<Boolean> SHOW_ARMS = getSingle("armor_stand_show_arms", false).withValidInput("true", "false");
    public final SingleProperty<Boolean> HAS_BASE_PLATE = getSingle("has_base_plate", true)
            .withValidInput("true", "false");
    public final SingleProperty<Boolean> SMALL = getSingle("small", false)
            .withValidInput("true", "false");

    public final SingleProperty<Vector3f> HEAD_ROTATION = getSingle("head_rotation", Vector3f.zero());
    public final SingleProperty<Vector3f> BODY_ROTATION = getSingle("body_rotation", Vector3f.zero());
    public final SingleProperty<Vector3f> RIGHT_ARM_ROTATION = getSingle("right_arm_rotation", Vector3f.zero());
    public final SingleProperty<Vector3f> LEFT_ARM_ROTATION = getSingle("left_arm_rotation", Vector3f.zero());
    public final SingleProperty<Vector3f> RIGHT_LEG_ROTATION = getSingle("right_leg_rotation", Vector3f.zero());
    public final SingleProperty<Vector3f> LEFT_LEG_ROTATION = getSingle("left_leg_rotation", Vector3f.zero());

    public ArmorStandProperties()
    {
        registerSingle(SHOW_ARMS, HAS_BASE_PLATE, SMALL);

        registerSingle(HEAD_ROTATION, BODY_ROTATION, RIGHT_ARM_ROTATION, LEFT_ARM_ROTATION, RIGHT_LEG_ROTATION, LEFT_LEG_ROTATION);
    }

    @Override
    protected @Nullable Pair<SingleProperty<?>, Object> parseSingleInput(String key, String value)
    {
        if (key.equals(SHOW_ARMS.id()))
            return Pair.of(SHOW_ARMS, Boolean.valueOf(value));
        else if (key.equals(HAS_BASE_PLATE.id()))
            return Pair.of(HAS_BASE_PLATE, Boolean.valueOf(value));
        else if (key.equals(SMALL.id()))
            return Pair.of(SMALL, Boolean.valueOf(value));

        return super.parseSingleInput(key, value);
    }

    @Override
    protected @Nullable ArmorStand tryCastEntity(@Nullable Entity targetEntity)
    {
        return targetEntity instanceof ArmorStand armorStand ? armorStand : null;
    }

    @Override
    public void setupPropertiesFromEntity(PropertyHandler propertyHandler, @NotNull ArmorStand armorStand)
    {
        propertyHandler.set(SHOW_ARMS, armorStand.hasArms());
        propertyHandler.set(HAS_BASE_PLATE, armorStand.hasBasePlate());
        propertyHandler.set(SMALL, armorStand.isSmall());

        propertyHandler.set(HEAD_ROTATION, fromRotations(armorStand.getHeadRotations()));
        propertyHandler.set(BODY_ROTATION, fromRotations(armorStand.getBodyRotations()));
        propertyHandler.set(LEFT_ARM_ROTATION, fromRotations(armorStand.getLeftArmRotations()));
        propertyHandler.set(RIGHT_ARM_ROTATION, fromRotations(armorStand.getRightArmRotations()));
        propertyHandler.set(LEFT_LEG_ROTATION, fromRotations(armorStand.getLeftLegRotations()));
        propertyHandler.set(RIGHT_LEG_ROTATION, fromRotations(armorStand.getRightLegRotations()));
    }

    public Vector3f fromRotations(Rotations rotations)
    {
        return new Vector3f((float)rotations.x(), (float)rotations.y(), (float)rotations.z());
    }

    @Override
    protected void setupDefaultProperties(PropertyHandler propertyHandler)
    {
    }

    @Override
    public Map<String, String> mapToNetworkProperties(PropertyHandler propertyHandler)
    {
        return Map.of(
                "show_arms", propertyHandler.get(SHOW_ARMS).toString()
        );
    }
}
