package xyz.nifeather.morph.misc.disguiseProperty;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class SingleProperty<T>
{
    private final String identifier;
    private final T defaultVal;
    private final Class<T> type;
    private final InputHandle<T> inputHandle;
    private final OutputHandle<T> outputHandle;
    private final IPropertyValidator<T> propertyValidator;
    private final boolean hideFromUserInput;

    public String id()
    {
        return identifier;
    }

    public T defaultVal()
    {
        return defaultVal;
    }

    public Class<T> type()
    {
        return type;
    }

    public boolean hideFromUserInput()
    {
        return hideFromUserInput;
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

    public SingleProperty(String identifier, T defaultValue, Class<T> type,
                          @NotNull InputHandle<T> inputHandle, @NotNull OutputHandle<T> outputHandle,
                          @NotNull IPropertyValidator<T> validator,
                          boolean hideFromUserInput)
    {
        this.identifier = identifier;
        this.defaultVal = defaultValue;
        this.type = type;
        this.inputHandle = inputHandle;
        this.outputHandle = outputHandle;
        this.propertyValidator = validator;
        this.hideFromUserInput = hideFromUserInput;
    }

    private final List<String> validValues = new CopyOnWriteArrayList<>();

    @Unmodifiable
    public List<String> validInputs()
    {
        return new ObjectArrayList<>(validValues);
    }

    public SingleProperty<T> withValidInput(Collection<String> input)
    {
        this.validValues.addAll(input);

        return this;
    }
    public SingleProperty<T> withValidInput(String... input)
    {
        this.validValues.addAll(Arrays.stream(input).toList());

        return this;
    }

    private final List<T> randomValues = new CopyOnWriteArrayList<>();

    @Unmodifiable
    public List<T> getRandomValues()
    {
        return new ObjectArrayList<>(randomValues);
    }

    public SingleProperty<T> withRandom(Collection<T> values)
    {
        this.randomValues.clear();
        this.randomValues.addAll(values);

        return this;
    }

    public SingleProperty<T> withRandom(T... randomValues)
    {
        return withRandom(Arrays.stream(randomValues).toList());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof SingleProperty<?> other)) return false;

        return this.identifier.equals(other.identifier) && this.type.equals(other.type);
    }

    public static <T> SingleProperty<T> of(String id, T val, InputHandle<T> inputHandle, OutputHandle<T> outputHandle)
    {
        return SingleProperty.builder(id, (Class<T>) val.getClass(), val)
                .withInputHandle(inputHandle)
                .withOutputHandle(outputHandle)
                .build();
    }

    public static <T> SingleProperty<T> of(String id, T val, InputHandle<T> inputHandle, OutputHandle<T> outputHandle, boolean hideFromUserInput)
    {
        return SingleProperty.builder(id, (Class<T>) val.getClass(), val)
                .withInputHandle(inputHandle)
                .withOutputHandle(outputHandle)
                .withHideFromUserInput(hideFromUserInput)
                .build();
    }

    public static <T> SingleProperty<T> of(String id, T val, Class<T> type, InputHandle<T> inputHandle, OutputHandle<T> outputHandle, boolean hideFromUserInput)
    {
        return SingleProperty.builder(id, type, val)
                .withInputHandle(inputHandle)
                .withOutputHandle(outputHandle)
                .withHideFromUserInput(hideFromUserInput)
                .build();
    }

    public static <X> SinglePropertyBuilder<X> builder(String id, Class<X> type, X defaultVal)
    {
        return new SinglePropertyBuilder<>(id, type, defaultVal);
    }

    public static class SinglePropertyBuilder<X>
    {
        private final String identifier;
        private final X defaultVal;
        private final Class<X> type;
        private InputHandle<X> inputHandle = InputHandles::empty;
        private OutputHandle<X> outputHandle = OutputHandles::immediateException;
        private IPropertyValidator<X> validator = PropertyValidations::noOp;
        private boolean hideFromUserInput = false;

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

        public SinglePropertyBuilder<X> withValidator(IPropertyValidator<X> validator)
        {
            this.validator = validator;
            return this;
        }

        public SinglePropertyBuilder<X> withHideFromUserInput(boolean hideFromUserInput)
        {
            this.hideFromUserInput = hideFromUserInput;
            return this;
        }

        public SingleProperty<X> build()
        {
            return new SingleProperty<>(this.identifier, this.defaultVal, this.type, inputHandle, outputHandle, validator, hideFromUserInput);
        }
    }
}
