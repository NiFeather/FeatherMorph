package xyz.nifeather.morph.misc.disguiseProperty;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.papermc.paper.math.Rotations;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.DyeColor;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import xyz.nifeather.morph.messages.ExceptionStrings;

import java.util.*;

public class InputHandles
{
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public static <X> Optional<X> empty(String propertyName, String ignored)
    {
        return Optional.empty();
    }

    public static <X> Optional<X> unsupported(String propertyName, String ignored) throws ParseErrorException
    {
        throw ParseErrorException.forProperty(propertyName)
                .byMethod("unsupported")
                .withMessage("This property is not available for this disguise")
                .withLocalizableMessage(ExceptionStrings.unsupported())
                .create();
    }

    public static <X> Optional<X> immediateException(String propertyName, String ignored) throws ParseErrorException
    {
        throw ParseErrorException.forProperty(propertyName)
                .byMethod("immediateException")
                .withMessage("This poor property does not accept any inputs")
                .withLocalizableMessage(ExceptionStrings.noUserInput())
                .create();
    }

    public static <X> Optional<X> reservedException(String propertyName, String ignored) throws ParseErrorException
    {
        throw ParseErrorException.forProperty(propertyName)
                .byMethod("reservedException")
                .withMessage("Internal property, not available for user inputs")
                .withLocalizableMessage(ExceptionStrings.internalProperty())
                .create();
    }

    public static Optional<Boolean> readBooleanStrict(String propertyName, String input) throws ParseErrorException
    {
        if (input.isBlank())
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readBooleanStrict")
                    .withMessage("Empty input for a boolean type")
                    .withLocalizableMessage(ExceptionStrings.noEmptyInput())
                    .create();
        }

        return Optional.of(Boolean.parseBoolean(input));
    }

    public static Optional<Boolean> readBooleanRelaxed(String propertyName, String input) throws ParseErrorException
    {
        if (input.isBlank())
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readBooleanRelaxed")
                    .withMessage("Empty input for a boolean type")
                    .withLocalizableMessage(ExceptionStrings.noEmptyInput())
                    .create();
        }

        if (input.equalsIgnoreCase("true")
            || input.equalsIgnoreCase("yes")
            || input.equals("1")
            || input.equalsIgnoreCase("y")
            || input.equalsIgnoreCase("t"))
        {
            return Optional.of(true);
        }
        else
        {
            return Optional.of(false);
        }
    }

    public static Optional<Integer> readInteger(String propertyName, String input) throws ParseErrorException
    {
        try
        {
            return Optional.of(Integer.parseInt(input));
        }
        catch (Throwable t)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readInteger")
                    .withLocalizableMessage(ExceptionStrings.failedParsingWhatFromInput().resolve("type", "Integer"))
                    .withMessage("Can't parse integer from input '%s'".formatted(input))
                    .causedBy(t)
                    .create();
        }
    }

    public static <E extends Enum<?>> Optional<E> readEnum(E[] array, String propertyName, String input)
    {
        return Arrays.stream(array)
                .filter(e -> e.name().equalsIgnoreCase(input))
                .findFirst();
    }

    //region Enum

    public static <E extends Enum<?>> Optional<E> readEnumNonNull(E[] array, String propertyName, String input) throws ParseErrorException
    {
        if (array.length == 0)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readEnumNonNull")
                    .withLocalizableMessage(ExceptionStrings.emptyValueCandidate())
                    .withMessage("Empty enum array! Is the server bugged?")
                    .create();
        }

        var optional = Arrays.stream(array)
                .filter(e -> e.name().equalsIgnoreCase(input))
                .findFirst();

        if (optional.isPresent()) return optional;

        throw ParseErrorException.forProperty(propertyName)
                .byMethod("readEnumNonNull")
                .withLocalizableMessage(ExceptionStrings.noValueMatch())
                .withMessage("No value match for input '%s'".formatted(input))
                .create();
    }

    public static Optional<DyeColor> readDyeColor(String propertyName, String input) throws ParseErrorException
    {
        return readEnumNonNull(DyeColor.values(), propertyName, input);
    }

    //endregion Enum
    public static Optional<Component> readAdventureComponentLimitedNonEmpty(String propertyName, String string) throws ParseErrorException
    {
        if (string.isBlank())
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readAdventureComponentLimited")
                    .withMessage("Blank string for component")
                    .withLocalizableMessage(ExceptionStrings.noEmptyInput())
                    .create();
        }

        return readAdventureComponentLimited(propertyName, string);
    }

    public static Optional<Component> readAdventureComponentLimited(String propertyName, String string) throws ParseErrorException
    {
        if (string.length() > 256)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readAdventureComponentLimited")
                    .withMessage("Given input is too long!")
                    .withLocalizableMessage(ExceptionStrings.inputTooLong())
                    .create();
        }

        var componentOptional = InputHandles.readAdventureComponent(propertyName, string);

        if (componentOptional.isPresent())
        {
            var finalText = PlainTextComponentSerializer.plainText().serialize(componentOptional.get());

            if (finalText.length() > 50)
            {
                throw ParseErrorException.forProperty(propertyName)
                        .byMethod("readAdventureComponentLimited")
                        .withMessage("The parse result is too long!")
                        .withLocalizableMessage(ExceptionStrings.inputTooLong())
                        .create();
            }

            if (finalText.isBlank())
            {
                throw ParseErrorException.forProperty(propertyName)
                        .byMethod("readAdventureComponentLimited")
                        .withMessage("Blank component is not allowed")
                        .withLocalizableMessage(ExceptionStrings.noEmptyInput())
                        .create();
            }
        }

        return componentOptional;
    }

    public static Optional<Component> readAdventureComponent(String propertyName, String input) throws ParseErrorException
    {
        try
        {
            return Optional.of(MiniMessage.miniMessage().deserialize(input));
        }
        catch (Throwable t)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readAdventureComponent")
                    .withLocalizableMessage(ExceptionStrings.failedParsingWhatFromInput().resolve("type", "Text Component"))
                    .withMessage("Can't parse input to adventure component from value '%s'".formatted(input))
                    .causedBy(t)
                    .create();
        }
    }

    public static Optional<UUID> readUUID(String propertyName, String input) throws ParseErrorException
    {
        try
        {
            return Optional.of(UUID.fromString(input));
        }
        catch (Throwable t)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readUUID")
                    .withLocalizableMessage(ExceptionStrings.failedParsingWhatFromInput().resolve("type", "UUID"))
                    .withMessage("Can't read UUID from input '%s'".formatted(input))
                    .causedBy(t)
                    .create();
        }
    }

    //region Registry

    public static <V extends Keyed> Optional<V> readRegistry(RegistryKey<@NotNull V> registryKey, String propertyName, String input) throws ParseErrorException
    {
        var key = NamespacedKey.fromString(input);
        if (key == null)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readRegistry")
                    .withLocalizableMessage(ExceptionStrings.failedParsingWhatFromInput().resolve("type", "ResourceLocation"))
                    .withMessage("Invalid input for ResourceLocation: '%s'".formatted(input))
                    .create();
        }

        Registry<@NotNull V> registry;

        try
        {
            registry = RegistryAccess.registryAccess().getRegistry(registryKey);
        }
        catch (NoSuchElementException e)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readRegistry")
                    .withLocalizableMessage(ExceptionStrings.emptyValueCandidate())
                    .withMessage("Can't read from registry since the target registry '%s' does not exist! Is the server broken?".formatted(registryKey.key().toString()))
                    .causedBy(e)
                    .create();
        }
        catch (IllegalArgumentException e)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readRegistry")
                    .withLocalizableMessage(ExceptionStrings.registryNotAvailable())
                    .withMessage("The target registry '%s' is not available at this moment. Is the server broken?".formatted(registryKey.key().toString()))
                    .causedBy(e)
                    .create();
        }

        try
        {
            var val = registry.getOrThrow(key);

            return Optional.of(val);
        }
        catch (NoSuchElementException e)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readRegistry")
                    .withLocalizableMessage(ExceptionStrings.noValueMatch())
                    .withMessage("Can't read value from input '%s' in registry '%s'".formatted(input, registryKey.key().toString()))
                    .causedBy(e)
                    .create();
        }
    }

    public static Optional<Villager.Type> readVillagerType(String propertyName, String input) throws ParseErrorException
    {
        return readRegistry(RegistryKey.VILLAGER_TYPE, propertyName, input);
    }

    public static Optional<Villager.Profession> readVillagerProfession(String propertyName, String input) throws ParseErrorException
    {
        return readRegistry(RegistryKey.VILLAGER_PROFESSION, propertyName, input);
    }

    //endregion Registry

    public static Optional<Integer> readVillagerLevel(String propertyName, String string) throws ParseErrorException
    {
        // localizable message not required, since readInteger always return a value or throw ParseErrorException
        var val = InputHandles.readInteger(propertyName, string)
                .orElseThrow(() -> new ParseErrorException(propertyName, "readVillagerLevel: Unable to parse villager level from input '%s'".formatted(string)));

        return Optional.of(Math.clamp(val, 1, 6));
    }

    public static class RotationStore
    {
        private float x, y, z;

        public void x(float v) { x = v; }
        public void y(float v) { y = v; }
        public void z(float v) { z = v; }

        public Rotations toRotations()
        {
            return Rotations.ofDegrees(x, y, z);
        }
    }

    public static Optional<Float> readFloatStrict(String propertyName, String input) throws ParseErrorException
    {
        float v;
        try
        {
            v = Float.parseFloat(input);
        }
        catch (NumberFormatException e)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readFloatStrict")
                    .withLocalizableMessage(ExceptionStrings.failedParsingWhatFromInput().resolve("type", "Float"))
                    .withMessage("Can't parse float from input '%s': %s".formatted(input, e.getMessage()))
                    .causedBy(e)
                    .create();
        }

        if (!Float.isFinite(v))
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readFloatStrict")
                    .withLocalizableMessage(ExceptionStrings.nonFinite())
                    .withMessage("Non-Finite value from input '%s'".formatted(input))
                    .create();
        }

        return Optional.of(v);
    }

    public static Optional<Rotations> readRotations(String propertyName, String value) throws ParseErrorException
    {
        if (value.isBlank())
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readRotations")
                    .withMessage("Empty input for a rotation type")
                    .withLocalizableMessage(ExceptionStrings.noEmptyInput())
                    .create();
        }

        try
        {
            var list = gson.fromJson(value, List.class);

            RotationStore rotationStore = new RotationStore();

            if (!list.isEmpty())
                readFloatStrict(propertyName, "" + list.get(0)).ifPresent(rotationStore::x);

            if (list.size() > 1)
                readFloatStrict(propertyName,"" + list.get(1)).ifPresent(rotationStore::y);

            if (list.size() > 2)
                readFloatStrict(propertyName,"" + list.get(2)).ifPresent(rotationStore::z);

            return Optional.of(rotationStore.toRotations());
        }
        catch (ParseErrorException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readRotations")
                    .withLocalizableMessage(ExceptionStrings.failedParsingWhatFromInput().resolve("type", "Float array"))
                    .withMessage("readRotations: Failed to parse float array in JSON from input '%s'".formatted(value))
                    .causedBy(e)
                    .create();
        }
    }

    public static Optional<String> readSkinName(String propertyName, String value) throws ParseErrorException
    {
        if (propertyName.length() > 16)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readSkinName")
                    .withMessage("Input name exceeds the limit of 16 characters")
                    .withLocalizableMessage(ExceptionStrings.inputTooLong())
                    .create();
        }

        return Optional.of(value);
    }

    public static void throwIfOutOfBounds(String propertyName, int value, int min, int max) throws ParseErrorException
    {
        if (value < min || value > max)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("throwIfOutOfBounds")
                    .withLocalizableMessage(ExceptionStrings.outOfRangeClosedBracket().resolve("min", min).resolve("max", max))
                    .withMessage("Input '%s' does not fit the required range of [%s, %s]".formatted(value, min, max))
                    .create();
        }
    }
}
