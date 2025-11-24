package xyz.nifeather.morph.misc.disguiseProperty;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.GameProfile;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.math.Rotations;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.DyeColor;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.*;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.nifeather.morph.messages.strings.ExceptionStrings;
import xyz.nifeather.morph.messages.strings.TypesString;
import xyz.nifeather.morph.misc.DisguiseEquipment;
import xyz.nifeather.morph.misc.disguiseProperty.struct.MorphEquipmentStruct;
import xyz.nifeather.morph.misc.disguiseProperty.struct.MorphProfileProperty;
import xyz.nifeather.morph.misc.disguiseProperty.struct.MorphResolvableProfileStruct;
import xyz.nifeather.morph.misc.skins.PlayerSkinProvider;
import xyz.nifeather.morph.network.utils.ProtocolEquipmentSlot;
import xyz.nifeather.morph.utilities.GameProfileUtils;
import xyz.nifeather.morph.utilities.NbtUtils;

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
                    .withLocalizableMessage(ExceptionStrings.failedParsingWhatFromInput().resolve("type", TypesString.typeInteger()))
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

    /**
     * Read {@link Component} from the given input, in JSON or MiniMessage format. <br>
     * Since we only utilizes JSON and MiniMessage, so this is called "any"
     */
    public static Optional<Component> readComponentAny(String propertyName, String value) throws ParseErrorException
    {
        if (value.startsWith("{"))
            return readJSONComponentLimitedNonEmpty(propertyName, value);
        else
            return readAdventureComponentLimitedNonEmpty(propertyName, value);
    }

    public static Optional<Component> readJSONComponentLimitedNonEmpty(String propertyName, String string) throws ParseErrorException
    {
        try
        {
            var json = JSONComponentSerializer.json().deserialize(string);
            throwIfIllegal(propertyName, json);

            return Optional.of(json);
        }
        catch (JsonParseException e)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readJSONComponentLimitedNonEmpty")
                    .causedBy(e)
                    .withMessage("Invalid JSON Component")
                    .withLocalizableMessage(ExceptionStrings.malformedInput())
                    .create();
        }
    }

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
            throwIfIllegal(propertyName, componentOptional.get());

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
                    .withLocalizableMessage(ExceptionStrings.failedParsingWhatFromInput().resolve("type", TypesString.textComponent()))
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
                    .withLocalizableMessage(ExceptionStrings.failedParsingWhatFromInput().resolve("type", TypesString.resourceLocation()))
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
        private double x, y, z;

        public void x(double v) { x = v; }
        public void y(double v) { y = v; }
        public void z(double v) { z = v; }

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
                    .withLocalizableMessage(ExceptionStrings.failedParsingWhatFromInput().resolve("type", TypesString.typeFloat()))
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

    public static Optional<Double> validateDoubleNullable(String propertyName, @Nullable Double value)
            throws ParseErrorException
    {
        if (value == null)
            return Optional.empty();

        if (!Double.isFinite(value))
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("validateDoubleNullable")
                    .withLocalizableMessage(ExceptionStrings.nonFinite())
                    .withMessage("Non-Finite value: %s".formatted(value))
                    .create();
        }

        return Optional.of(value);
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
            var list = gson.fromJson(value, new TypeToken<List<Double>>(){});

            RotationStore rotationStore = new RotationStore();

            if (list.size() > 3)
            {
                throw ParseErrorException.forProperty(propertyName)
                        .byMethod("readRotations")
                        .withLocalizableMessage(ExceptionStrings.inputTooMany().resolve("max", 3))
                        .withMessage("Too many elements for reading a Rotation!")
                        .create();
            }

            if (!list.isEmpty())
                validateDoubleNullable(propertyName, list.get(0)).ifPresent(rotationStore::x);

            if (list.size() > 1)
                validateDoubleNullable(propertyName, list.get(1)).ifPresent(rotationStore::y);

            if (list.size() > 2)
                validateDoubleNullable(propertyName, list.get(2)).ifPresent(rotationStore::z);

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
                    .withLocalizableMessage(ExceptionStrings.failedParsingWhatFromInput().resolve("type", TypesString.collectionOf().resolve("what", TypesString.typeFloat())))
                    .withMessage("readRotations: Failed to parse float array in JSON from input '%s'".formatted(value))
                    .causedBy(e)
                    .create();
        }
    }

    public static Optional<ResolvableProfile> readResolvableSkinInput(String propertyName, String value) throws ParseErrorException
    {
        if (value.startsWith("{"))
        {
            return readResolvableProfile(propertyName, value);
        }
        else
        {
            if (value.length() > 16)
            {
                throw ParseErrorException.forProperty(propertyName)
                        .byMethod("readResolvableSkinInput")
                        .withMessage("Input name exceeds the limit of 16 characters")
                        .withLocalizableMessage(ExceptionStrings.inputTooLong())
                        .create();
            }

            var resolvable = PlayerSkinProvider.getInstance().getCachedProfileOptional(value)
                    .map(skin ->
                    {
                        return ResolvableProfile.resolvableProfile(GameProfileUtils.asPlayerProfile(skin));
                    }).orElseGet(() -> ResolvableProfile.resolvableProfile().name(value).build());

            return Optional.of(resolvable);
        }
    }

    public static Optional<GameProfile> readGameProfile(String propertyName, String value) throws ParseErrorException
    {
        return Optional.of(NbtUtils.readGameProfileOrThrow(value));
    }

    public static Optional<ResolvableProfile> readResolvableProfile(String propertyName, String input) throws ParseErrorException
    {
        try
        {
            var record = gson.fromJson(input, MorphResolvableProfileStruct.class);

            return record.dynamic()
                    ? readResolvableProfileDynamic(propertyName, record)
                    : readResolvableProfileStatic(propertyName, record);
        }
        catch (JsonSyntaxException e)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readResolvableProfile")
                    .causedBy(e)
                    .withMessage("Possibly Malformed JSON")
                    .withLocalizableMessage(ExceptionStrings.malformedInput())
                    .create();
        }
    }

    public static Optional<ResolvableProfile> readResolvableProfileDynamic(String propertyName, MorphResolvableProfileStruct record) throws ParseErrorException
    {
        if (record.id() != null)
            return Optional.of(ResolvableProfile.resolvableProfile().uuid(record.id()).build());
        else if (record.name() != null)
            return Optional.of(ResolvableProfile.resolvableProfile().name(record.name()).build());

        throw ParseErrorException.forProperty(propertyName)
                .byMethod("readResolvableProfileDynamic")
                .withMessage("Unable to create dynamic ResolvableProfile: Either UUID and name is NULL")
                .withLocalizableMessage(ExceptionStrings.malformedInput())
                .create();
    }

    public static Optional<ResolvableProfile> readResolvableProfileStatic(String propertyName, MorphResolvableProfileStruct record) throws ParseErrorException
    {
        if (record.name() == null || record.id() == null)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .byMethod("readResolvableProfileStatic")
                    .withMessage("UUID or Name of the record is NULL")
                    .withLocalizableMessage(ExceptionStrings.malformedInput())
                    .create();
        }

        var builder = ResolvableProfile.resolvableProfile();
        builder.name(record.name());
        builder.uuid(record.id());

        var profilePropertiesBuilder = new ArrayList<ProfileProperty>();

        // properties
        //ImmutableMultimap.Builder<String, Property> propertiesBuilder = ImmutableMultimap.builder();
        for (String propertyJson : record.properties())
        {
            try
            {
                var struct = gson.fromJson(propertyJson, MorphProfileProperty.class);
                profilePropertiesBuilder.add(new ProfileProperty(struct.name(), struct.value(), struct.signature()));
            }
            catch (JsonParseException e)
            {
                throw ParseErrorException.forProperty(propertyName)
                        .causedBy(e)
                        .byMethod("readResolvableProfileStatic")
                        .withMessage("GSON error, see details")
                        .withLocalizableMessage(ExceptionStrings.malformedInput())
                        .create();
            }
        }

        builder.addProperties(profilePropertiesBuilder);

        //skin patch
        var skinPatchBuilder = ResolvableProfile.SkinPatch.skinPatch();
        nullableStringToKey(record.cape()).ifPresent(skinPatchBuilder::cape);
        nullableStringToKey(record.bodyTexture()).ifPresent(skinPatchBuilder::body);
        nullableStringToKey(record.elytra()).ifPresent(skinPatchBuilder::elytra);

        if (record.model() != null)
            readEnumNonNull(PlayerTextures.SkinModel.values(), "SkinModel", record.model()).ifPresent(skinPatchBuilder::model);

        builder.skinPatch(skinPatchBuilder.build());

        return Optional.of(builder.build());
    }

    public static Optional<Key> nullableStringToKey(@Nullable String str)
    {
        return str == null ? Optional.empty() : Optional.of(Key.key(str));
    }

    public static Optional<DisguiseEquipment> readEquipment(String propertyName, String input) throws ParseErrorException
    {
        MorphEquipmentStruct struct = null;
        try
        {
            struct = gson.fromJson(input, MorphEquipmentStruct.class);
        }
        catch (JsonParseException e)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .causedBy(e)
                    .withLocalizableMessage(ExceptionStrings.malformedInput())
                    .withMessage("Unable to decode JSON string")
                    .create();
        }

        if (struct == null)
        {
            throw ParseErrorException.forProperty(propertyName)
                    .withLocalizableMessage(ExceptionStrings.noEmptyInput())
                    .withMessage("Empty input")
                    .create();
        }

        int dataVersion = struct.dataVersion();

        var builder = DisguiseEquipment.builder(Map.of());

        // Let's treat this as empty map
        if (struct.equipmentData() == null)
            return Optional.of(builder.build());

        for (Map.Entry<String, String> entry : struct.equipmentData().entrySet())
        {
            String slotName = entry.getKey();
            String snbt = entry.getValue();

            var protocolSlot = ProtocolEquipmentSlot.valueOf(slotName.toUpperCase());
            var item = NbtUtils.readItemStack(snbt, dataVersion);

            org.bukkit.inventory.EquipmentSlot slot = switch (protocolSlot)
            {
                case MAINHAND -> org.bukkit.inventory.EquipmentSlot.HAND;
                case OFF_HAND -> org.bukkit.inventory.EquipmentSlot.OFF_HAND;

                case HELMET -> org.bukkit.inventory.EquipmentSlot.HEAD;
                case CHESTPLATE -> org.bukkit.inventory.EquipmentSlot.CHEST;
                case LEGGINGS ->  org.bukkit.inventory.EquipmentSlot.LEGS;
                case BOOTS -> org.bukkit.inventory.EquipmentSlot.FEET;
            };

            if (item != null)
                builder.forSlot(slot, item);
        }

        return Optional.of(builder.build());
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

    public static void throwIfIllegal(String propertyName, Component component) throws ParseErrorException
    {
        var finalText = PlainTextComponentSerializer.plainText().serialize(component);

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
}
