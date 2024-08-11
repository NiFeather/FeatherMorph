package xiamomc.morph.backends.server.renderer.network.datawatcher.values;

import com.github.retrooper.packetevents.protocol.entity.data.EntityDataType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SingleValue<T>
{
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

    @NotNull
    private EntityDataType<T> dataType;

    public void setDataType(@NotNull EntityDataType<T> newType)
    {
        Objects.requireNonNull(newType, "Can't pass a null type to a value('%s')!".formatted(this.name()));

        this.dataType = newType;
    }

    @NotNull
    public EntityDataType<T> getDataType()
    {
        if (dataType == null)
            throw new NullPointerException("Someone forgot to set the data type for this value('%s')!".formatted(this.name()));

        return dataType;
    }

    private final String name;

    public String name()
    {
        return name;
    }

    public SingleValue(String name, EntityDataType<T> dataType, int index, @NotNull T defaultValue)
    {
        this.name = name;
        this.setDataType(dataType);
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

        return this.index == other.index && this.dataType.equals(other.dataType);
    }

    @Override
    public String toString()
    {
        return "SingleValue[name='%s', type='%s', index='%s']".formatted(name, dataType, index);
    }

    public static <TVal> SingleValue<TVal> of(String name, int index, @NotNull TVal val, EntityDataType<TVal> dataType)
    {
        if (val == null)
            throw new IllegalArgumentException("TVal may not be null");

        return new SingleValue<>(name, dataType, index, val);
    }
}
