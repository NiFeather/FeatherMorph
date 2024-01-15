package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class SingleValue<T>
{
    private final Class<T> type;

    public Class<T> type()
    {
        return type;
    }

    private final int index;

    public int index()
    {
        return index;
    }

    private final T defaultValue;

    @NotNull
    public T defaultValue()
    {
        return defaultValue;
    }

    @Nullable
    private WrappedDataWatcher.Serializer serializer;

    public void setSerializer(WrappedDataWatcher.Serializer serializer)
    {
        this.serializer = serializer;
    }

    @Nullable
    public WrappedDataWatcher.Serializer getSerializer()
    {
        return serializer;
    }

    private final String name;

    public String name()
    {
        return name;
    }

    public SingleValue(String name, Class<T> type, int index, @NotNull T defaultValue)
    {
        this.name = name;
        this.type = type;
        this.index = index;
        this.defaultValue = defaultValue;
    }

    private final List<T> randomValues = new ObjectArrayList<>();

    public List<T> getRandomValues()
    {
        return new ObjectArrayList<>(randomValues);
    }

    public SingleValue<T> withRandom(List<T> values)
    {
        this.randomValues.clear();
        this.randomValues.addAll(values);

        return this;
    }

    public SingleValue<T> withRandom(T... randomValues)
    {
        return withRandom(Arrays.stream(randomValues).toList());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof SingleValue<?> other)) return false;

        return this.index == other.index && this.type.equals(other.type);
    }

    @Override
    public String toString()
    {
        return "SingleValue[name='%s', type='%s', index='%s']".formatted(name, type, index);
    }

    public static <TVal> SingleValue<TVal> of(String name, int index, @NotNull TVal val)
    {
        if (val == null)
            throw new IllegalArgumentException("TVal may not be null");

        return new SingleValue<>(name, (Class<TVal>) val.getClass(), index, val);
    }
}
