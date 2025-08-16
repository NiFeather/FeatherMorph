package xyz.nifeather.morph.misc.disguiseProperty;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class SingleProperty<T>
{
    private final String identifier;
    private final T defaultVal;
    private final Class<T> type;
    private final Function<String, Optional<T>> inputHandle;

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

    public Optional<T> forInput(String input)
    {
        return inputHandle.apply(input);
    }

    @Deprecated
    public SingleProperty(String identifier, T defaultValue, Class<T> type)
    {
        this(identifier, defaultValue, type, null);
    }

    public SingleProperty(String identifier, T defaultValue, Class<T> type, @Nullable Function<String, Optional<T>> inputHandle)
    {
        if (inputHandle == null)
            inputHandle = str -> Optional.empty();

        this.identifier = identifier;
        this.defaultVal = defaultValue;
        this.type = type;
        this.inputHandle = inputHandle;
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

    public static <T> SingleProperty<T> of(String id, T val)
    {
        return new SingleProperty<>(id, val, (Class<T>) val.getClass());
    }

    public static <T> SingleProperty<T> of(String id, T val, Function<String, Optional<T>> inputHandle)
    {
        return new SingleProperty<>(id, val, (Class<T>) val.getClass(), inputHandle);
    }
}
