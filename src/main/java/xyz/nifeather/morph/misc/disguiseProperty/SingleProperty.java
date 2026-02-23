package xyz.nifeather.morph.misc.disguiseProperty;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public record SingleProperty<T>(String identifier, T defaultVal, Class<T> type, InputHandle<T> inputHandle,
                                OutputHandle<T> outputHandle, IPropertyValidator<T> propertyValidator, IPostProcessHandle<T> postProcessHandle,
                                List<T> randomValues, List<String> suggestions,
                                boolean hideFromUserInput, boolean hideFromClient)
{
    public SingleProperty(String identifier, T defaultVal, Class<T> type,
                          @NotNull InputHandle<T> inputHandle, @NotNull OutputHandle<T> outputHandle,
                          @NotNull IPropertyValidator<T> propertyValidator, IPostProcessHandle<T> postProcessHandle,
                          List<T> randomValues, List<String> suggestions,
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

    public void validateInput(T value, Player player, EnumSet<ValidationFlag> validationFlags)
            throws PropertyValidationException
    {
        this.propertyValidator.validate(value, player, validationFlags);
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
         * @see PropertyHandler#toNetworkProperties()
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

        public SingleProperty<X> build()
        {
            return new SingleProperty<>(this.identifier, this.defaultVal, this.type,
                    inputHandle, outputHandle,
                    validator, postProcessHandle,
                    randomValues, suggestions,
                    hideFromUserInput, hideFromClient);
        }
    }
}
