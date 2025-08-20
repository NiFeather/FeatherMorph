package xyz.nifeather.morph.misc.disguiseProperty;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.papermc.paper.math.Rotations;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import it.unimi.dsi.fastutil.Function;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.DyeColor;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InputHandles
{
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public static <X> Optional<X> empty(String ignored)
    {
        return Optional.empty();
    }

    public static Optional<Boolean> readBooleanStrict(String input) throws ParseErrorException
    {
        if (input.isBlank())
            throw new ParseErrorException("readBooleanStrict: Empty input for a boolean type");

        return Optional.of(Boolean.parseBoolean(input));
    }

    public static Optional<Boolean> readBooleanRelaxed(String input) throws ParseErrorException
    {
        if (input.isBlank())
            throw new ParseErrorException("readBooleanRelaxed: Empty input for a boolean type");

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

    public static Optional<Integer> readInteger(String input) throws ParseErrorException
    {
        try
        {
            return Optional.of(Integer.parseInt(input));
        }
        catch (Throwable t)
        {
            throw new ParseErrorException("readInteger: Can't parse integer from input '%s'".formatted(input), t);
        }
    }

    public static <E extends Enum<?>> Optional<E> readEnum(E[] array, String input)
    {
        return Arrays.stream(array)
                .filter(e -> e.name().equalsIgnoreCase(input))
                .findFirst();
    }

    //region Enum

    public static <E extends Enum<?>> Optional<E> readEnumNonNull(E[] array, String input) throws ParseErrorException
    {
        if (array.length == 0)
            throw new ParseErrorException("readEnumNonNull: Empty enum array! Is the server bugged?");

        var optional = Arrays.stream(array)
                .filter(e -> e.name().equalsIgnoreCase(input))
                .findFirst();

        if (optional.isPresent()) return optional;
        else throw new ParseErrorException("readEnumNonNull: No value match for input '%s'".formatted(input));
    }

    public static Optional<DyeColor> readDyeColor(String input) throws ParseErrorException
    {
        return readEnumNonNull(DyeColor.values(), input);
    }

    //endregion Enum

    public static Optional<Component> readAdventureComponent(String input) throws ParseErrorException
    {
        try
        {
            return Optional.of(MiniMessage.miniMessage().deserialize(input));
        }
        catch (Throwable t)
        {
            throw new ParseErrorException("readAdventureComponent: Can't parse input to adventure component from value '%s'".formatted(input), t);
        }
    }

    public static Optional<UUID> readUUID(String input) throws ParseErrorException
    {
        try
        {
            return Optional.of(UUID.fromString(input));
        }
        catch (Throwable t)
        {
            throw new ParseErrorException("readUUID: Can't read UUID from input '%s'".formatted(input), t);
        }
    }

    //region Registry

    public static <V extends Keyed> Optional<V> readRegistry(RegistryKey<@NotNull V> registryKey, String input) throws ParseErrorException
    {
        var key = NamespacedKey.fromString(input);
        if (key == null) return Optional.empty();

        try
        {
            var registry = RegistryAccess.registryAccess().getRegistry(registryKey);
            var val = registry.getOrThrow(key);

            return Optional.of(val);
        }
        catch (Throwable t)
        {
            throw new ParseErrorException("readRegistry: Can't read value from input '%s' in registry '%s'".formatted(input, registryKey.key().toString()), t);
        }
    }

    public static Optional<Villager.Type> readVillagerType(String input) throws ParseErrorException
    {
        return readRegistry(RegistryKey.VILLAGER_TYPE, input);
    }

    public static Optional<Villager.Profession> readVillagerProfession(String input) throws ParseErrorException
    {
        return readRegistry(RegistryKey.VILLAGER_PROFESSION, input);
    }

    //endregion Registry

    public static Optional<Integer> readVillagerLevel(String string) throws ParseErrorException
    {
        var val = InputHandles.readInteger(string)
                .orElseThrow(() -> new ParseErrorException("readVillagerLevel: Unable to parse villager level from input '%s'".formatted(string)));

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

    public static Optional<Float> readFloatStrict(String input) throws ParseErrorException
    {
        float v;
        try
        {
            v = Float.parseFloat(input);
        }
        catch (Throwable t)
        {
            throw new ParseErrorException("readFloatStrict: Can't parse float from input '%s': %s".formatted(input, t.getMessage()), t);
        }

        if (!Float.isFinite(v))
            throw new ParseErrorException("readFloatStrict: Non-Finite value from input '%s'".formatted(input));

        return Optional.of(v);
    }

    public static Optional<Rotations> readRotations(String value) throws ParseErrorException
    {
        if (value.isBlank())
            throw new ParseErrorException("readRotations: Empty input for a rotation type");

        try
        {
            var list = gson.fromJson(value, List.class);

            RotationStore rotationStore = new RotationStore();

            if (!list.isEmpty())
                readFloatStrict("" + list.get(0)).ifPresent(rotationStore::x);

            if (list.size() > 1)
                readFloatStrict("" + list.get(1)).ifPresent(rotationStore::y);

            if (list.size() > 2)
                readFloatStrict("" + list.get(2)).ifPresent(rotationStore::z);

            return Optional.of(rotationStore.toRotations());
        }
        catch (ParseErrorException e)
        {
            throw e;
        }
        catch (Throwable t)
        {
            throw new ParseErrorException("readRotations: Failed to parse float array in JSON from input '%s'".formatted(value), t);
        }
    }

    public static void throwIfOutOfBounds(int value, int min, int max) throws ParseErrorException
    {
        if (value < min || value > max)
            throw new ParseErrorException("throwIfOutOfBounds: Input '%s' does not fit the required range of [%s, %s]".formatted(value, min, max));
    }
}
