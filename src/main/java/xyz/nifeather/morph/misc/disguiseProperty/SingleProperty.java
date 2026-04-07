package xyz.nifeather.morph.misc.disguiseProperty;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

/**
 * A disguise property
 * @param identifier The identifier(ID) of this property.
 * @param defaultVal Default value for this property.
 * @param type Class type of the default value.
 * @param inputHandle An {@link InputHandle} which deserializes the input String to an instance of the type.
 * @param outputHandle An {@link OutputHandle} which serializes the value to String.
 * @param propertyValidator An {@link IPropertyValidator} to validate if the value is legit, and the player has permission to use this property.
 * @param postProcessHandle An {@link IPostProcessHandle}, used to make changes to the {@link PropertyHandler} for properties that has difficult to directly apply to the disguise. For example: {@link xyz.nifeather.morph.misc.disguiseProperty.values.HappyGhastPropertyCollection#HARNESS}
 * @param randomValues Available random values for this property. Mostly used by PropertyCollections. For example: {@link xyz.nifeather.morph.misc.disguiseProperty.values.AxolotlPropertyCollection#setupDefaultProperties(PropertyHandler)}
 * @param suggestions Available suggestions for this property.
 * @param restoreDefaultsBeforeDiscard Whether we should restore the default value before we discard the property.
 * @param hideFromUserInput {@code true} if this property should be hidden in places like Command Suggestions.
 * @param hideFromClient {@code true} if this property should not be sent to the client when syncing properties.
 */
public record SingleProperty<T>(String identifier, T defaultVal, Class<T> type, InputHandle<T> inputHandle,
                                OutputHandle<T> outputHandle, IPropertyValidator<T> propertyValidator, IPostProcessHandle<T> postProcessHandle,
                                List<T> randomValues, List<String> suggestions, boolean restoreDefaultsBeforeDiscard,
                                boolean hideFromUserInput, boolean hideFromClient)
{
    public SingleProperty(String identifier, T defaultVal, Class<T> type,
                          @NotNull InputHandle<T> inputHandle, @NotNull OutputHandle<T> outputHandle,
                          @NotNull IPropertyValidator<T> propertyValidator, IPostProcessHandle<T> postProcessHandle,
                          List<T> randomValues, List<String> suggestions, boolean restoreDefaultsBeforeDiscard,
                          boolean hideFromUserInput, boolean hideFromClient)
    {
        this.identifier = identifier;
        this.defaultVal = defaultVal;
        this.type = type;

        this.inputHandle = inputHandle;
        this.outputHandle = outputHandle;
        this.propertyValidator = propertyValidator;

        // Need to move this to another better place, as I don't want SingleProperty containing codes that can directly interact with PropertyHandler.
        // But what else place should we move?
        // - PropertyCollection is only for adding properties.
        // - Adding to DisguiseProvider would just pollute them.
        //
        // Maybe the design of the Post Process Handle is bad, but I have no idea on how to make this better.
        // Since we need to have a way to let players customize Happy Ghast Disguise's saddle. :(
        this.postProcessHandle = postProcessHandle;

        this.restoreDefaultsBeforeDiscard = restoreDefaultsBeforeDiscard;
        this.hideFromUserInput = hideFromUserInput;
        this.hideFromClient = hideFromClient;

        this.randomValues = ImmutableList.copyOf(randomValues);
        this.suggestions = ImmutableList.copyOf(suggestions);
    }

    public String id()
    {
        return identifier;
    }

    public Optional<T> forInput(String input) throws ParseErrorException
    {
        return inputHandle.handle(this.id(), input);
    }

    @NotNull
    public String forValue(T value) throws ParseErrorException
    {
        return outputHandle.handle(this.id(), value);
    }

    public void validateInput(T value, Entity player, EnumSet<ValidationSkipFlag> validationSkipFlags)
            throws PropertyValidationException
    {
        this.propertyValidator.validate(value, player, validationSkipFlags);
    }

    @Unmodifiable
    public List<String> validInputs()
    {
        return new ObjectArrayList<>(suggestions);
    }

    @Override
    @Unmodifiable
    public List<T> randomValues()
    {
        return new ObjectArrayList<>(randomValues);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof SingleProperty<?> other)) return false;

        return this.identifier.equals(other.identifier) && this.type.equals(other.type);
    }

    @Override
    public int hashCode()
    {
        // There shouldn't be two properties having the same name but different types or any other.
        // If that really happens, well... someone should blame the dev who made this happen.
        return identifier.hashCode();
    }

    public static <X> SinglePropertyBuilder<X> builder(String id, X defaultVal)
    {
        return new SinglePropertyBuilder<>(id, (Class<X>) defaultVal.getClass(), defaultVal);
    }

    public static <X> SinglePropertyBuilder<X> builder(String id, Class<X> type, X defaultVal)
    {
        return new SinglePropertyBuilder<>(id, type, defaultVal);
    }

    public static class SinglePropertyBuilder<X>
    {
        private static final IPostProcessHandle<Object> defaultPostProcessHandle = (a, b) -> {};

        private final String identifier;
        private final X defaultVal;
        private final Class<X> type;
        private InputHandle<X> inputHandle = InputHandles::empty;
        private OutputHandle<X> outputHandle = OutputHandles::immediateException;
        private IPropertyValidator<X> validator = PropertyValidations::noOp;
        private boolean hideFromUserInput = false;
        private boolean hideFromClient = false;
        private IPostProcessHandle<X> postProcessHandle = (IPostProcessHandle<X>) defaultPostProcessHandle;
        private boolean restoreDefaultsBeforeDiscard = true;

        public SinglePropertyBuilder(String identifier, Class<X> type, X defaultVal)
        {
            this.identifier = identifier;
            this.type = type;
            this.defaultVal = defaultVal;
        }

        public SinglePropertyBuilder<X> withInputHandle(InputHandle<X> handle)
        {
            this.inputHandle = handle;
            return this;
        }

        public SinglePropertyBuilder<X> withOutputHandle(OutputHandle<X> handle)
        {
            this.outputHandle = handle;
            return this;
        }

        /**
         * Sets the validation method for this property.
         * @see IPropertyValidator
         */
        public SinglePropertyBuilder<X> withValidator(IPropertyValidator<X> validator)
        {
            this.validator = validator;
            return this;
        }

        public SinglePropertyBuilder<X> withPostProcess(IPostProcessHandle<X> postProcessHandle)
        {
            this.postProcessHandle = postProcessHandle;
            return this;
        }

        /**
         * Whether this property should not be visible for player input.
         * @apiNote This flag doesn't prevent this property from being parsed, to achieve that, use {@link InputHandles#immediateException(String, String)} as the input handle
         */
        public SinglePropertyBuilder<X> hideFromUserInput(boolean hideFromUserInput)
        {
            this.hideFromUserInput = hideFromUserInput;
            return this;
        }

        /**
         * Whether this property should not be visible to clients.
         * <br>
         * Properties with this flag would not get synced to clients via plugin message.
         *
         * @see PropertyHandler#serializeNonTempProperties()
         */
        public SinglePropertyBuilder<X> hideFromClient(boolean hideFromClient)
        {
            this.hideFromClient = hideFromClient;
            return this;
        }

        private final List<X> randomValues = new ObjectArrayList<>();

        @SafeVarargs
        public final SinglePropertyBuilder<X> withRandom(X... values)
        {
            randomValues.addAll(Arrays.stream(values).toList());
            return this;
        }

        public SinglePropertyBuilder<X> withRandom(Collection<X> values)
        {
            randomValues.addAll(values);
            return this;
        }

        private final List<String> suggestions = new ObjectArrayList<>();

        public SinglePropertyBuilder<X> withSuggestions(String... values)
        {
            suggestions.addAll(Arrays.stream(values).toList());
            return this;
        }

        public SinglePropertyBuilder<X> withSuggestions(Collection<String> values)
        {
            suggestions.addAll(values);
            return this;
        }

        public SinglePropertyBuilder<X> restoreDefaultsBeforeDiscard(boolean v)
        {
            this.restoreDefaultsBeforeDiscard = v;
            return this;
        }

        public SingleProperty<X> build()
        {
            return new SingleProperty<>(this.identifier, this.defaultVal, this.type,
                    inputHandle, outputHandle,
                    validator, postProcessHandle,
                    randomValues, suggestions, restoreDefaultsBeforeDiscard,
                    hideFromUserInput, hideFromClient);
        }
    }
}
