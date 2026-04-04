package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Pose;
import org.jetbrains.annotations.ApiStatus;
import xyz.nifeather.morph.messages.strings.CommandStrings;
import xyz.nifeather.morph.messages.strings.ExceptionStrings;
import xyz.nifeather.morph.misc.disguiseProperty.*;
import xyz.nifeather.morph.misc.permissions.CommonPermissions;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;

public abstract class EntityPropertyCollection<E extends Entity> extends PropertyCollection<E>
{
    public final SingleProperty<Float> STATIC_YAW = SingleProperty.builder(PropertyNames.ENTITY_STATIC_YAW, 0f)
            .withInputHandle(this::readYaw)
            .withOutputHandle(OutputHandles::writeFloat)
            .build();

    public final SingleProperty<Float> STATIC_PITCH = SingleProperty.builder(PropertyNames.ENTITY_STATIC_PITCH, 0f)
            .withInputHandle(this::readPitch)
            .withOutputHandle(OutputHandles::writeFloat)
            .build();

    @ApiStatus.Experimental
    public final SingleProperty<Pose> STATIC_POSE = SingleProperty.builder(PropertyNames.ENTITY_STATIC_POSE, Pose.STANDING)
            .withInputHandle(this::readPose)
            .withOutputHandle(OutputHandles::writeEnumOrdinal) // Bukkit has different name comparing to vanilla Minecraft 🫠
            .withSuggestions(Arrays.stream(Pose.values()).map(p -> p.name().toLowerCase()).toList())
            .withValidator(this::validatePosePermission)
            .build();

    private void validatePosePermission(Pose pose, Entity entity, EnumSet<ValidationSkipFlag> skipFlags)
            throws PropertyValidationException
    {
        if (skipFlags.contains(ValidationSkipFlag.SKIP_PERMISSIONS))
            return;

        if (pose == Pose.STANDING
                || pose == Pose.SNEAKING
                || pose == Pose.FALL_FLYING
                || pose == Pose.SLEEPING)
        {
            return;
        }

        if (!entity.hasPermission(CommonPermissions.LIMITED_POSES))
        {
            throw PropertyValidationException.forProperty(PropertyNames.ENTITY_STATIC_POSE)
                    .withLocalizableMessage(CommandStrings.noPermissionMessage())
                    .withMessage("Player don't have permission to set limited poses.")
                    .create();
        }
    }

    private Optional<Pose> readPose(String propertyName, String input)
            throws ParseErrorException
    {
        try
        {
            int index = Integer.parseInt(input);
            var values = Pose.values();

            if (index >= values.length)
            {
                throw ParseErrorException.forProperty(propertyName)
                        .withLocalizableMessage(ExceptionStrings.noValueMatch())
                        .withMessage("Pose does not contain an enum with index %s".formatted(index))
                        .create();
            }

            return Optional.of(values[index]);
        }
        catch (NumberFormatException _)
        {
        }

        return InputHandles.readEnumNonNull(Pose.values(), propertyName, input);
    }

    private Optional<Float> readYaw(String propertyName, String input)
            throws ParseErrorException
    {
        var raw = InputHandles.readFloatStrict(propertyName, input).orElse(null);
        if (raw == null)
            return Optional.empty();

        if (Math.abs(raw) > 360f)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("EntityPropertyCollection#readYaw")
                    .withLocalizableMessage(ExceptionStrings.outOfRangeClosedBracket().resolve("min", -360).resolve("max", 360))
                    .create();
        }

        return Optional.of(raw);
    }

    private Optional<Float> readPitch(String propertyName, String input)
            throws ParseErrorException
    {
        var raw = InputHandles.readFloatStrict(propertyName, input).orElse(null);
        if (raw == null)
            return Optional.empty();

        if (Math.abs(raw) > 90f)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("EntityPropertyCollection#readPitch")
                    .withLocalizableMessage(ExceptionStrings.outOfRangeClosedBracket().resolve("min", -90).resolve("max", 90))
                    .create();
        }

        return Optional.of(raw);
    }

    public EntityPropertyCollection()
    {
        registerSingle(STATIC_YAW, STATIC_PITCH, STATIC_POSE);
    }
}
