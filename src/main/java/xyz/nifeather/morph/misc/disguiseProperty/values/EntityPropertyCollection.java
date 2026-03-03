package xyz.nifeather.morph.misc.disguiseProperty.values;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Pose;
import xyz.nifeather.morph.messages.strings.ExceptionStrings;
import xyz.nifeather.morph.misc.disguiseProperty.*;

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

    public final SingleProperty<Pose> STATIC_POSE = SingleProperty.builder(PropertyNames.ENTITY_STATIC_POSE, Pose.STANDING)
            .withInputHandle(this::readPose)
            .withOutputHandle(OutputHandles::writeEnum)
            .build();

    private Optional<Pose> readPose(String propertyName, String input)
            throws ParseErrorException
    {
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
